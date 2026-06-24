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

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            if ("2031".equals(typeDoc)) {
                PdfContentByte over = stamper.getOverContent(1); // Page 1
                over.beginText();
                over.setFontAndSize(bf, 8);

                // Exercice fiscal (Y = 735)
                ecrireTexteAligne(over, "01/01/" + annee, 110, 735, PdfContentByte.ALIGN_LEFT);
                ecrireTexteAligne(over, "31/12/" + annee, 245, 735, PdfContentByte.ALIGN_LEFT);

                // Régime simplifié d'imposition (Coche à X = 513, Y = 735)
                ecrireTexteAligne(over, "X", 513, 735, PdfContentByte.ALIGN_LEFT);

                // Identification (Cadre A, Y = 688, 665, 642)
                String nomProprio = user.getPrenom() + " " + user.getNom();
                ecrireTexteAligne(over, nomProprio, 75, 688, PdfContentByte.ALIGN_LEFT);
                
                String nomLogement = logement.getNom() != null ? logement.getNom() : "";
                ecrireTexteAligne(over, "MyTiny - Tiny House " + nomLogement, 75, 665, PdfContentByte.ALIGN_LEFT);

                String adr = (logement.getAdresse() != null ? logement.getAdresse() : "") + " " +
                             (logement.getCodePostal() != null ? logement.getCodePostal() : "") + " " +
                             (logement.getVille() != null ? logement.getVille() : "");
                ecrireTexteAligne(over, adr, 75, 642, PdfContentByte.ALIGN_LEFT);

                // Résultat fiscal (Bénéfice Col 1 à X=492, Déficit Col 2 à X=566, Y=570)
                BigDecimal beneficeReel = bilan.resultatReelImposable();
                BigDecimal deficitReel = BigDecimal.ZERO;
                if (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise()).compareTo(BigDecimal.ZERO) < 0) {
                    deficitReel = (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise())).abs();
                }

                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(over, formatNombre(beneficeReel), 492, 570, PdfContentByte.ALIGN_RIGHT);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(over, formatNombre(deficitReel), 566, 570, PdfContentByte.ALIGN_RIGHT);
                }

                // Cadre 7 : dont BIC non professionnels (Bénéfice à X=492, Déficit à X=566, Y=238)
                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(over, formatNombre(beneficeReel), 492, 238, PdfContentByte.ALIGN_RIGHT);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(over, formatNombre(deficitReel), 566, 238, PdfContentByte.ALIGN_RIGHT);
                }

                over.endText();

                // Page 2 : Cadre I (BIC non professionnels)
                PdfContentByte overPage2 = stamper.getOverContent(2);
                overPage2.beginText();
                overPage2.setFontAndSize(bf, 8);

                // Report bénéfice/déficit dans cases correspondantes LMNP (Autres locations meublées non prof)
                // Ligne "Autres locations meublées non professionnelles" -> Y = 88
                // Ligne "Résultat avant imputation..." -> Y = 22
                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(overPage2, formatNombre(beneficeReel), 492, 88, PdfContentByte.ALIGN_RIGHT);
                    ecrireTexteAligne(overPage2, formatNombre(beneficeReel), 492, 22, PdfContentByte.ALIGN_RIGHT);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(overPage2, formatNombre(deficitReel), 566, 88, PdfContentByte.ALIGN_RIGHT);
                    ecrireTexteAligne(overPage2, formatNombre(deficitReel), 566, 22, PdfContentByte.ALIGN_RIGHT);
                }
                
                overPage2.endText();
            } 
            else if ("2033".equals(typeDoc)) {
                // Page 1 : 2033-A (Bilan simplifié)
                PdfContentByte overPage1 = stamper.getOverContent(1);
                overPage1.beginText();
                overPage1.setFontAndSize(bf, 8);

                // Identification (Y = 782)
                String designation = user.getPrenom() + " " + user.getNom() + " - Tiny " + (logement.getNom() != null ? logement.getNom() : "");
                ecrireTexteAligne(overPage1, designation, 105, 782, PdfContentByte.ALIGN_LEFT);
                ecrireTexteAligne(overPage1, "31/12/" + annee, 490, 732, PdfContentByte.ALIGN_LEFT);

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

                // Immobilisations corporelles (028, 030, 032) -> Y = 558
                ecrireTexteAligne(overPage1, formatNombre(immoBrut), 418, 558, PdfContentByte.ALIGN_RIGHT);
                ecrireTexteAligne(overPage1, formatNombre(immoAmort), 492, 558, PdfContentByte.ALIGN_RIGHT);
                ecrireTexteAligne(overPage1, formatNombre(immoNet), 566, 558, PdfContentByte.ALIGN_RIGHT);
                
                // Disponibilités (086 - Net) -> Y = 318
                ecrireTexteAligne(overPage1, formatNombre(dispos), 566, 318, PdfContentByte.ALIGN_RIGHT);
                
                // Total Actif (110, 112) -> Y = 254
                ecrireTexteAligne(overPage1, formatNombre(immoBrut.add(dispos)), 418, 254, PdfContentByte.ALIGN_RIGHT);
                ecrireTexteAligne(overPage1, formatNombre(immoAmort), 492, 254, PdfContentByte.ALIGN_RIGHT);
                ecrireTexteAligne(overPage1, formatNombre(immoNet.add(dispos)), 566, 254, PdfContentByte.ALIGN_RIGHT);

                // Passif
                BigDecimal beneficeReel = bilan.resultatReelImposable();
                BigDecimal deficitReel = BigDecimal.ZERO;
                if (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise()).compareTo(BigDecimal.ZERO) < 0) {
                    deficitReel = (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise())).abs();
                }
                BigDecimal resultPassif = beneficeReel.compareTo(BigDecimal.ZERO) > 0 ? beneficeReel : deficitReel.negate();
                BigDecimal capitalIndiv = immoNet.add(dispos).subtract(resultPassif);

                // Capital individuel (120) -> Y = 194
                ecrireTexteAligne(overPage1, formatNombre(capitalIndiv), 566, 194, PdfContentByte.ALIGN_RIGHT);
                // Résultat de l'exercice (136) -> Y = 148
                ecrireTexteAligne(overPage1, formatNombre(resultPassif), 566, 148, PdfContentByte.ALIGN_RIGHT);
                // Total Passif (180) -> Y = 78
                ecrireTexteAligne(overPage1, formatNombre(capitalIndiv.add(resultPassif)), 566, 78, PdfContentByte.ALIGN_RIGHT);

                overPage1.endText();

                // Page 2 : 2033-B (Compte de résultat)
                PdfContentByte overPage2 = stamper.getOverContent(2);
                overPage2.beginText();
                overPage2.setFontAndSize(bf, 8);

                // Chiffre d'affaires prestations (217) -> Y = 582
                ecrireTexteAligne(overPage2, formatNombre(bilan.recettesBrutes()), 566, 582, PdfContentByte.ALIGN_RIGHT);
                // Total produits d'exploitation (232) -> Y = 538
                ecrireTexteAligne(overPage2, formatNombre(bilan.recettesBrutes()), 566, 538, PdfContentByte.ALIGN_RIGHT);

                // Charges
                ecrireTexteAligne(overPage2, formatNombre(totalAchats), 566, 488, PdfContentByte.ALIGN_RIGHT); // Achats (238)
                ecrireTexteAligne(overPage2, formatNombre(totalAutresChargesExternes), 566, 458, PdfContentByte.ALIGN_RIGHT); // Charges externes (242)
                ecrireTexteAligne(overPage2, formatNombre(totalImpotsTaxes), 566, 441, PdfContentByte.ALIGN_RIGHT); // Impôts (244)
                ecrireTexteAligne(overPage2, formatNombre(bilan.amortissementUtilise()), 566, 408, PdfContentByte.ALIGN_RIGHT); // Amortissements (254)

                BigDecimal totalCharges = totalAchats.add(totalAutresChargesExternes).add(totalImpotsTaxes).add(bilan.amortissementUtilise());
                ecrireTexteAligne(overPage2, formatNombre(totalCharges), 566, 348, PdfContentByte.ALIGN_RIGHT); // Total charges (264)

                BigDecimal resultExploit = bilan.recettesBrutes().subtract(totalCharges);
                ecrireTexteAligne(overPage2, formatNombre(resultExploit), 566, 322, PdfContentByte.ALIGN_RIGHT); // Résultat d'exploitation (270)

                // Résultat fiscal (312 / 314) -> Y = 248
                if (resultExploit.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(overPage2, formatNombre(resultExploit), 492, 248, PdfContentByte.ALIGN_RIGHT);
                } else {
                    ecrireTexteAligne(overPage2, formatNombre(resultExploit.abs()), 566, 248, PdfContentByte.ALIGN_RIGHT);
                }

                // Déduction amortissements utilisés (350) -> Y = 178
                ecrireTexteAligne(overPage2, formatNombre(bilan.amortissementUtilise()), 566, 178, PdfContentByte.ALIGN_RIGHT);
                
                // Résultat fiscal final (352 / 354) -> Y = 108
                if (beneficeReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(overPage2, formatNombre(beneficeReel), 492, 108, PdfContentByte.ALIGN_RIGHT);
                } else if (deficitReel.compareTo(BigDecimal.ZERO) > 0) {
                    ecrireTexteAligne(overPage2, formatNombre(deficitReel), 566, 108, PdfContentByte.ALIGN_RIGHT);
                }

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

                // Tableau Immos (Constructions) -> Y = 678
                ecrireTexteAligne(overPage3, formatNombre(immoDebut), 325, 678, PdfContentByte.ALIGN_RIGHT); // Début (430)
                ecrireTexteAligne(overPage3, formatNombre(immoAug), 405, 678, PdfContentByte.ALIGN_RIGHT); // Augmentation (432)
                ecrireTexteAligne(overPage3, formatNombre(immoDebut.add(immoAug)), 565, 678, PdfContentByte.ALIGN_RIGHT); // Fin (436)

                // Tableau Amortissements (Constructions) -> Y = 498
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

                ecrireTexteAligne(overPage3, formatNombre(amortDebut), 325, 498, PdfContentByte.ALIGN_RIGHT); // Début (520)
                ecrireTexteAligne(overPage3, formatNombre(amortDot), 405, 498, PdfContentByte.ALIGN_RIGHT); // Dotation (522)
                ecrireTexteAligne(overPage3, formatNombre(amortDebut.add(amortDot)), 565, 498, PdfContentByte.ALIGN_RIGHT); // Fin (526)

                overPage3.endText();
            }

            stamper.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void ecrireTexteAligne(PdfContentByte over, String text, float x, float y, int alignement) {
        if (text == null) return;
        over.showTextAligned(alignement, text, x, y, 0);
    }

    private String formatNombre(BigDecimal val) {
        if (val == null) return "0";
        return String.format("%,.0f", val.setScale(0, RoundingMode.HALF_UP));
    }
}
