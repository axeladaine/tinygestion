package fr.denebolar.tinygestion.service;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.*;
import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeclarationPdfService {

    private final BilanFiscalService bilanFiscalService;
    private final DepenseRepository depenseRepository;
    private final BienAmortissableRepository bienAmortissableRepository;
    private final LogementService logementService;

    public byte[] genererDeclarationPdf(Long logementId, Integer annee, Utilisateur user) {
        Logement logement = logementService.getLogementById(logementId, user);
        BilanFiscalDto bilan = bilanFiscalService.calculerBilanFiscal(logementId, annee, user);

        LocalDate start = LocalDate.of(annee, 1, 1);
        LocalDate end = LocalDate.of(annee, 12, 31);
        List<Depense> depenses = depenseRepository.findByLogementIdAndDateDepenseBetween(logementId, start, end);
        List<BienAmortissable> biens = bienAmortissableRepository.findByLogementId(logementId);

        // Catégorisation des charges pour l'annexe 2033-B
        BigDecimal totalAchats = BigDecimal.ZERO;
        BigDecimal totalAutresChargesExternes = BigDecimal.ZERO;
        BigDecimal totalImpotsTaxes = BigDecimal.ZERO;

        for (Depense d : depenses) {
            if (d.getStatutDeductibilite() == StatutDeductibilite.DEDUCTIBLE) {
                BigDecimal montant = d.getMontantRetenu() != null ? d.getMontantRetenu() : BigDecimal.ZERO;
                String cat = d.getCategorie() != null ? d.getCategorie() : "";

                if (cat.contains("Électricité") || cat.contains("eau") || cat.contains("Consommables") || cat.contains("Linge de maison")) {
                    totalAchats = totalAchats.add(montant);
                } else if (cat.contains("Impôts") || cat.contains("CFE")) {
                    totalImpotsTaxes = totalImpotsTaxes.add(montant);
                } else {
                    totalAutresChargesExternes = totalAutresChargesExternes.add(montant);
                }
            }
        }

        // Templates PDF dans les ressources du projet
        String pdf2031Resource = "/templates/formulaire_2031_template.pdf";
        String pdf2033Resource = "/templates/formulaire_2033_template.pdf";

        byte[] bytes2031 = stamperDocument(pdf2031Resource, annee, logement, bilan, "2031", totalAchats, totalAutresChargesExternes, totalImpotsTaxes, biens, user);
        byte[] bytes2033 = stamperDocument(pdf2033Resource, annee, logement, bilan, "2033", totalAchats, totalAutresChargesExternes, totalImpotsTaxes, biens, user);

        // Concaténation des deux fichiers PDF générés en un seul document
        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfCopy copy = new PdfCopy(document, finalOut);
            document.open();

            // Formulaire 2031
            PdfReader reader2031 = new PdfReader(bytes2031);
            int pages2031 = reader2031.getNumberOfPages();
            for (int i = 1; i <= pages2031; i++) {
                copy.addPage(copy.getImportedPage(reader2031, i));
            }
            reader2031.close();

            // Annexes 2033
            PdfReader reader2033 = new PdfReader(bytes2033);
            int pages2033 = reader2033.getNumberOfPages();
            for (int i = 1; i <= pages2033; i++) {
                copy.addPage(copy.getImportedPage(reader2033, i));
            }
            reader2033.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return finalOut.toByteArray();
    }

    private byte[] stamperDocument(String templateResourcePath, Integer annee, Logement logement, BilanFiscalDto bilan, String typeDoc, BigDecimal totalAchats, BigDecimal totalAutresChargesExternes, BigDecimal totalImpotsTaxes, List<BienAmortissable> biens, Utilisateur user) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (java.io.InputStream templateStream = getClass().getResourceAsStream(templateResourcePath)) {
            if (templateStream == null) {
                throw new java.io.FileNotFoundException("Template introuvable : " + templateResourcePath);
            }
            PdfReader reader = new PdfReader(templateStream);
            PdfStamper stamper = new PdfStamper(reader, out);

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);

            if ("2031".equals(typeDoc)) {
                PdfContentByte over = stamper.getOverContent(1); // Page 1
                over.beginText();
                over.setFontAndSize(bf, 8);

                // Exercice fiscal
                ecrireTexte(over, "01/01/" + annee, 120, 155);
                ecrireTexte(over, "31/12/" + annee, 255, 155);

                // Régime simplifié d'imposition
                ecrireTexte(over, "X", 535, 155);

                // Identification
                String nomProprio = user.getPrenom() + " " + user.getNom();
                ecrireTexte(over, nomProprio, 75, 230);
                ecrireTexte(over, "MyTiny - Tiny House " + logement.getNom(), 75, 205);
                String adr = (logement.getAdresse() != null ? logement.getAdresse() : "") + " " +
                             (logement.getCodePostal() != null ? logement.getCodePostal() : "") + " " +
                             (logement.getVille() != null ? logement.getVille() : "");
                ecrireTexte(over, adr, 75, 180);

                // Résultat fiscal
                BigDecimal beneficeReel = bilan.resultatReelImposable();
                BigDecimal deficitReel = BigDecimal.ZERO;
                if (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise()).compareTo(BigDecimal.ZERO) < 0) {
                    deficitReel = (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise())).abs();
                }

                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexte(over, formatNombre(beneficeReel), 460, 310);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexte(over, formatNombre(deficitReel), 525, 310);
                }

                // Page 2 : Cadre I (BIC non professionnels)
                PdfContentByte overPage2 = stamper.getOverContent(2);
                overPage2.beginText();
                overPage2.setFontAndSize(bf, 8);

                // Identification
                ecrireTexte(overPage2, nomProprio, 230, 230);

                // Report bénéfice/déficit dans cases correspondantes LMNP
                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    // Case "Autres locations meublées non professionnelles" Bénéfice
                    ecrireTexte(overPage2, formatNombre(beneficeReel), 640, 115);
                    // Résultat avant imputation (total)
                    ecrireTexte(overPage2, formatNombre(beneficeReel), 640, 48);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    // Déficit
                    ecrireTexte(overPage2, formatNombre(deficitReel), 770, 115);
                    ecrireTexte(overPage2, formatNombre(deficitReel), 770, 48);
                }
                overPage2.endText();
                over.endText();
            } 
            else if ("2033".equals(typeDoc)) {
                // Page 1 : 2033-A (Bilan simplifie)
                PdfContentByte overPage1 = stamper.getOverContent(1);
                overPage1.beginText();
                overPage1.setFontAndSize(bf, 8);

                // Identification
                ecrireTexte(overPage1, user.getPrenom() + " " + user.getNom() + " - Tiny " + logement.getNom(), 105, 782);
                ecrireTexte(overPage1, "31/12/" + annee, 490, 732);

                // Calculs Actif
                BigDecimal immoBrut = biens.stream().map(b -> b.getMontantTtc() != null ? b.getMontantTtc() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal immoAmort = biens.stream().map(b -> {
                    LocalDate dateMiseEnService = b.getDateMiseEnService() != null ? b.getDateMiseEnService() : b.getDateAchat();
                    if (dateMiseEnService == null) return BigDecimal.ZERO;
                    int duree = b.getDureeAmortissementAns() != null ? b.getDureeAmortissementAns() : 1;
                    int anneesAmorties = Math.max(0, Math.min(duree, annee - dateMiseEnService.getYear() + 1));
                    BigDecimal annuel = b.getAmortissementAnnuel() != null ? b.getAmortissementAnnuel() : BigDecimal.ZERO;
                    return annuel.multiply(BigDecimal.valueOf(anneesAmorties));
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal immoNet = immoBrut.subtract(immoAmort);

                BigDecimal dispos = bilan.recettesBrutes().subtract(bilan.depensesRetenues());
                if (dispos.compareTo(BigDecimal.ZERO) < 0) dispos = BigDecimal.ZERO;

                // Immobilisations corporelles (028, 030, 032)
                ecrireTexte(overPage1, formatNombre(immoBrut), 480, 600);
                ecrireTexte(overPage1, formatNombre(immoAmort), 550, 600);
                ecrireTexte(overPage1, formatNombre(immoNet), 620, 600);
                // Disponibilités (084, 086)
                ecrireTexte(overPage1, formatNombre(dispos), 620, 475);
                // Total Actif (110, 112)
                ecrireTexte(overPage1, formatNombre(immoBrut.add(dispos)), 480, 420);
                ecrireTexte(overPage1, formatNombre(immoAmort), 550, 420);
                ecrireTexte(overPage1, formatNombre(immoNet.add(dispos)), 620, 420);

                // Passif
                BigDecimal beneficeReel = bilan.resultatReelImposable();
                BigDecimal deficitReel = BigDecimal.ZERO;
                if (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise()).compareTo(BigDecimal.ZERO) < 0) {
                    deficitReel = (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise())).abs();
                }
                BigDecimal resultPassif = beneficeReel.compareTo(BigDecimal.ZERO) > 0 ? beneficeReel : deficitReel.negate();
                BigDecimal capitalIndiv = immoNet.add(dispos).subtract(resultPassif);

                // Capital individuel (120)
                ecrireTexte(overPage1, formatNombre(capitalIndiv), 620, 330);
                // Résultat de l'exercice (136)
                ecrireTexte(overPage1, formatNombre(resultPassif), 620, 265);
                // Total Passif (180)
                ecrireTexte(overPage1, formatNombre(capitalIndiv.add(resultPassif)), 620, 130);

                overPage1.endText();

                // Page 2 : 2033-B (Compte de résultat)
                PdfContentByte overPage2 = stamper.getOverContent(2);
                overPage2.beginText();
                overPage2.setFontAndSize(bf, 8);

                // Chiffre d'affaires prestations (217)
                ecrireTexte(overPage2, formatNombre(bilan.recettesBrutes()), 620, 645);
                // Total produits d'exploitation (232)
                ecrireTexte(overPage2, formatNombre(bilan.recettesBrutes()), 620, 595);

                // Charges
                ecrireTexte(overPage2, formatNombre(totalAchats), 620, 525); // Achats (238)
                ecrireTexte(overPage2, formatNombre(totalAutresChargesExternes), 620, 495); // Charges externes (242)
                ecrireTexte(overPage2, formatNombre(totalImpotsTaxes), 620, 480); // Impôts (244)
                ecrireTexte(overPage2, formatNombre(bilan.amortissementUtilise()), 620, 445); // Amortissements (254)

                BigDecimal totalCharges = totalAchats.add(totalAutresChargesExternes).add(totalImpotsTaxes).add(bilan.amortissementUtilise());
                ecrireTexte(overPage2, formatNombre(totalCharges), 620, 385); // Total charges (264)

                BigDecimal resultExploit = bilan.recettesBrutes().subtract(totalCharges);
                ecrireTexte(overPage2, formatNombre(resultExploit), 620, 360); // Résultat d'exploitation (270)

                // Résultat fiscal (312 / 314)
                ecrireTexte(overPage2, formatNombre(resultExploit.compareTo(BigDecimal.ZERO) > 0 ? resultExploit : BigDecimal.ZERO), 620, 290);
                ecrireTexte(overPage2, formatNombre(resultExploit.compareTo(BigDecimal.ZERO) < 0 ? resultExploit.abs() : BigDecimal.ZERO), 620, 275);

                // Déduction amortissements utilisés (350)
                ecrireTexte(overPage2, formatNombre(bilan.amortissementUtilise()), 620, 205);
                // Résultat fiscal final (352 / 354)
                ecrireTexte(overPage2, formatNombre(beneficeReel), 620, 135);

                overPage2.endText();

                // Page 3 : 2033-C (Immobilisations & Amortissements)
                PdfContentByte overPage3 = stamper.getOverContent(3);
                overPage3.beginText();
                overPage3.setFontAndSize(bf, 8);

                BigDecimal immoDebut = BigDecimal.ZERO;
                BigDecimal immoAug = BigDecimal.ZERO;
                for (BienAmortissable b : biens) {
                    LocalDate dateAchat = b.getDateAchat() != null ? b.getDateAchat() : LocalDate.now();
                    BigDecimal val = b.getMontantTtc() != null ? b.getMontantTtc() : BigDecimal.ZERO;
                    if (dateAchat.getYear() == annee) {
                        immoAug = immoAug.add(val);
                    } else if (dateAchat.getYear() < annee) {
                        immoDebut = immoDebut.add(val);
                    }
                }

                // Tableau Immos (Constructions)
                ecrireTexte(overPage3, formatNombre(immoDebut), 340, 615); // Début (430)
                ecrireTexte(overPage3, formatNombre(immoAug), 410, 615); // Augmentation (432)
                ecrireTexte(overPage3, formatNombre(immoDebut.add(immoAug)), 560, 615); // Fin (436)

                // Tableau Amortissements (Constructions)
                BigDecimal amortDebut = BigDecimal.ZERO;
                BigDecimal amortDot = bilan.amortissementUtilise();
                for (BienAmortissable b : biens) {
                    LocalDate dateMiseEnService = b.getDateMiseEnService() != null ? b.getDateMiseEnService() : b.getDateAchat();
                    if (dateMiseEnService == null) continue;
                    int duree = b.getDureeAmortissementAns() != null ? b.getDureeAmortissementAns() : 1;
                    BigDecimal annuel = b.getAmortissementAnnuel() != null ? b.getAmortissementAnnuel() : BigDecimal.ZERO;
                    int anneesAmortiesAvant = Math.max(0, Math.min(duree, annee - dateMiseEnService.getYear()));
                    amortDebut = amortDebut.add(annuel.multiply(BigDecimal.valueOf(anneesAmortiesAvant)));
                }

                ecrireTexte(overPage3, formatNombre(amortDebut), 340, 435); // Début (520)
                ecrireTexte(overPage3, formatNombre(amortDot), 410, 435); // Dotation (522)
                ecrireTexte(overPage3, formatNombre(amortDebut.add(amortDot)), 560, 435); // Fin (526)

                overPage3.endText();
            }

            stamper.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void ecrireTexte(PdfContentByte over, String text, float x, float y) {
        if (text == null) return;
        over.showTextAligned(PdfContentByte.ALIGN_LEFT, text, x, y, 0);
    }

    private String formatNombre(BigDecimal val) {
        if (val == null) return "0";
        return String.format("%,.0f", val.setScale(0, RoundingMode.HALF_UP));
    }
}
