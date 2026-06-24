package fr.denebolar.tinygestion.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
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
        // 1. Récupération des données nécessaires
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Polices de caractères
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font fontNormalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fontMini = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);
            Font fontCodeCase = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLUE);

            Color colorHeader = new Color(51, 65, 85); // Slate 700
            Color colorBorder = new Color(226, 232, 240); // Slate 200

            // ==========================================
            // PAGE 1 : FORMULAIRE 2031-SD (RÉCAPITULATION)
            // ==========================================
            genererEnTeteCerfa(document, "N° 2031-SD", "11085*28", "DÉCLARATION DE RÉSULTAT BIC - LMNP", annee, fontTitre, fontSousTitre, fontMini);

            // Cadre Identification
            document.add(new Paragraph("A. IDENTIFICATION DU DECLARANT ET DU LOGEMENT", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableIdent = new PdfPTable(2);
            tableIdent.setWidthPercentage(100);
            tableIdent.setSpacingAfter(15);

            tableIdent.addCell(creerCellule("Propriétaire Exploitant :\n" + user.getPrenom() + " " + user.getNom() + "\nEmail: " + user.getEmail(), fontNormal, false, colorBorder));
            tableIdent.addCell(creerCellule("Tiny House concernée :\n" + logement.getNom() + "\nAdresse : " + 
                    (logement.getAdresse() != null ? logement.getAdresse() : "") + " " +
                    (logement.getCodePostal() != null ? logement.getCodePostal() : "") + " " +
                    (logement.getVille() != null ? logement.getVille() : ""), fontNormal, false, colorBorder));

            document.add(tableIdent);

            // Exercice fiscal
            PdfPTable tableEx = new PdfPTable(4);
            tableEx.setWidthPercentage(100);
            tableEx.setSpacingAfter(15);
            tableEx.addCell(creerCelluleHeader("Exercice ouvert le", fontNormalBold, colorHeader));
            tableEx.addCell(creerCellule("01/01/" + annee, fontNormal, true, colorBorder));
            tableEx.addCell(creerCelluleHeader("Exercice clos le", fontNormalBold, colorHeader));
            tableEx.addCell(creerCellule("31/12/" + annee, fontNormal, true, colorBorder));
            document.add(tableEx);

            // Cadre C : RÉCAPITULATION DES ÉLÉMENTS D'IMPOSITION (Régime Réel)
            document.add(new Paragraph("C. RÉCAPITULATION DES ÉLÉMENTS D'IMPOSITION", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableImposition = new PdfPTable(4);
            tableImposition.setWidthPercentage(100);
            tableImposition.setWidths(new float[]{40, 15, 22, 23});
            tableImposition.setSpacingAfter(20);

            // Headers
            tableImposition.addCell(creerCelluleHeader("Élément d'imposition", fontNormalBold, colorHeader));
            tableImposition.addCell(creerCelluleHeader("Code Case", fontNormalBold, colorHeader));
            tableImposition.addCell(creerCelluleHeader("Bénéfice (Col. 1)", fontNormalBold, colorHeader));
            tableImposition.addCell(creerCelluleHeader("Déficit (Col. 2)", fontNormalBold, colorHeader));

            // Résultat Fiscal Réel
            BigDecimal beneficeReel = bilan.resultatReelImposable();
            BigDecimal deficitReel = BigDecimal.ZERO;
            if (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise()).compareTo(BigDecimal.ZERO) < 0) {
                deficitReel = (bilan.resultatAvantAmortissement().subtract(bilan.amortissementUtilise())).abs();
            }

            tableImposition.addCell(creerCellule("1. Résultat fiscal de l'exercice (BIC)", fontNormal, false, colorBorder));
            tableImposition.addCell(creerCelluleCodeCase("370 / 372", fontCodeCase, colorBorder));
            tableImposition.addCell(creerCelluleMontant(beneficeReel, fontNormalBold, colorBorder));
            tableImposition.addCell(creerCelluleMontant(deficitReel, fontNormalBold, colorBorder));

            // LMNP (BIC non professionnel)
            tableImposition.addCell(creerCellule("7. Dont BIC non professionnels (LMNP)", fontNormal, false, colorBorder));
            tableImposition.addCell(creerCelluleCodeCase("7a / 7b", fontCodeCase, colorBorder));
            tableImposition.addCell(creerCelluleMontant(beneficeReel, fontNormalBold, colorBorder));
            tableImposition.addCell(creerCelluleMontant(deficitReel, fontNormalBold, colorBorder));

            document.add(tableImposition);

            // Cadre d'information fiscal / Arbitrage
            PdfPTable tableArbitrage = new PdfPTable(1);
            tableArbitrage.setWidthPercentage(100);
            tableArbitrage.setSpacingAfter(20);
            String messageArbitrage = "Arbitrage fiscal : Le régime fiscal préconisé pour votre exercice est le ";
            if ("REGIME_REEL_AVANTAGEUX".equals(bilan.regimeFiscalConseille())) {
                messageArbitrage += "RÉGIME RÉEL SIMPLIFIÉ (économie estimée de " + formatEuro(bilan.differenceGainImposable()) + " de base imposable par rapport au Micro-BIC).";
            } else {
                messageArbitrage += "MICRO-BIC (économie estimée de " + formatEuro(bilan.differenceGainImposable()) + " de base imposable par rapport au Réel).";
            }
            PdfPCell cellArb = creerCellule(messageArbitrage, fontNormalBold, false, colorBorder);
            cellArb.setBackgroundColor(new Color(240, 253, 250)); // Teal 50
            cellArb.setPadding(10);
            tableArbitrage.addCell(cellArb);
            document.add(tableArbitrage);

            // Signature
            document.add(new Paragraph("Fait à _________________________, le ____/____/_______", fontNormal));
            document.add(new Paragraph("Signature du déclarant :", fontNormal));

            // Nouvelle Page
            document.newPage();

            // ==========================================
            // PAGE 2 : ANNEXE 2033-A (BILAN SIMPLIFIÉ)
            // ==========================================
            genererEnTeteCerfa(document, "N° 2033-A-SD", "15948*08", "BILAN SIMPLIFIÉ - ACTIF ET PASSIF", annee, fontTitre, fontSousTitre, fontMini);

            document.add(new Paragraph("ACTIF SIMPLIFIÉ", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableActif = new PdfPTable(5);
            tableActif.setWidthPercentage(100);
            tableActif.setWidths(new float[]{45, 12, 14, 14, 15});
            tableActif.setSpacingAfter(15);

            tableActif.addCell(creerCelluleHeader("Rubrique Actif", fontNormalBold, colorHeader));
            tableActif.addCell(creerCelluleHeader("Code", fontNormalBold, colorHeader));
            tableActif.addCell(creerCelluleHeader("Brut", fontNormalBold, colorHeader));
            tableActif.addCell(creerCelluleHeader("Amortissements", fontNormalBold, colorHeader));
            tableActif.addCell(creerCelluleHeader("Net", fontNormalBold, colorHeader));

            // Valeurs Actif
            BigDecimal immoBrut = biens.stream().map(b -> b.getMontantTtc() != null ? b.getMontantTtc() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal immoAmort = biens.stream().map(b -> {
                // Amortissement cumulé incluant cette année
                LocalDate dateMiseEnService = b.getDateMiseEnService() != null ? b.getDateMiseEnService() : b.getDateAchat();
                if (dateMiseEnService == null) return BigDecimal.ZERO;
                int duree = b.getDureeAmortissementAns() != null ? b.getDureeAmortissementAns() : 1;
                int anneesAmorties = Math.max(0, Math.min(duree, annee - dateMiseEnService.getYear() + 1));
                BigDecimal annuel = b.getAmortissementAnnuel() != null ? b.getAmortissementAnnuel() : BigDecimal.ZERO;
                return annuel.multiply(BigDecimal.valueOf(anneesAmorties));
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal immoNet = immoBrut.subtract(immoAmort);

            // Immobilisations corporelles
            tableActif.addCell(creerCellule("Immobilisations corporelles", fontNormal, false, colorBorder));
            tableActif.addCell(creerCelluleCodeCase("028 / 030", fontCodeCase, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoBrut, fontNormal, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoAmort, fontNormal, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoNet, fontNormalBold, colorBorder));

            // Disponibilités (Actif Circulant)
            // Estimation simplifiée : Recettes - Dépenses accumulées (trésorerie)
            BigDecimal dispos = bilan.recettesBrutes().subtract(bilan.depensesRetenues());
            if (dispos.compareTo(BigDecimal.ZERO) < 0) dispos = BigDecimal.ZERO;

            tableActif.addCell(creerCellule("Disponibilités (Trésorerie)", fontNormal, false, colorBorder));
            tableActif.addCell(creerCelluleCodeCase("084 / 086", fontCodeCase, colorBorder));
            tableActif.addCell(creerCelluleMontant(dispos, fontNormal, colorBorder));
            tableActif.addCell(creerCelluleMontant(BigDecimal.ZERO, fontNormal, colorBorder));
            tableActif.addCell(creerCelluleMontant(dispos, fontNormalBold, colorBorder));

            // TOTAL GENERAL
            tableActif.addCell(creerCellule("TOTAL GÉNÉRAL DE L'ACTIF", fontNormalBold, false, colorBorder));
            tableActif.addCell(creerCelluleCodeCase("110 / 112", fontCodeCase, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoBrut.add(dispos), fontNormalBold, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoAmort, fontNormalBold, colorBorder));
            tableActif.addCell(creerCelluleMontant(immoNet.add(dispos), fontNormalBold, colorBorder));

            document.add(tableActif);

            document.add(new Paragraph("PASSIF SIMPLIFIÉ", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tablePassif = new PdfPTable(3);
            tablePassif.setWidthPercentage(100);
            tablePassif.setWidths(new float[]{60, 15, 25});
            tablePassif.setSpacingAfter(15);

            tablePassif.addCell(creerCelluleHeader("Rubrique Passif", fontNormalBold, colorHeader));
            tablePassif.addCell(creerCelluleHeader("Code", fontNormalBold, colorHeader));
            tablePassif.addCell(creerCelluleHeader("Montant Net", fontNormalBold, colorHeader));

            // Valeurs Passif
            // Capital individuel estimé (Équilibrage actif net - résultat)
            BigDecimal resultPassif = beneficeReel.compareTo(BigDecimal.ZERO) > 0 ? beneficeReel : deficitReel.negate();
            BigDecimal capitalIndiv = immoNet.add(dispos).subtract(resultPassif);

            tablePassif.addCell(creerCellule("Capital individuel / Compte de l'exploitant", fontNormal, false, colorBorder));
            tablePassif.addCell(creerCelluleCodeCase("120", fontCodeCase, colorBorder));
            tablePassif.addCell(creerCelluleMontant(capitalIndiv, fontNormal, colorBorder));

            tablePassif.addCell(creerCellule("Résultat de l'exercice (Bénéfice ou Déficit)", fontNormal, false, colorBorder));
            tablePassif.addCell(creerCelluleCodeCase("136", fontCodeCase, colorBorder));
            tablePassif.addCell(creerCelluleMontant(resultPassif, fontNormalBold, colorBorder));

            tablePassif.addCell(creerCellule("TOTAL GÉNÉRAL DU PASSIF", fontNormalBold, false, colorBorder));
            tablePassif.addCell(creerCelluleCodeCase("180", fontCodeCase, colorBorder));
            tablePassif.addCell(creerCelluleMontant(capitalIndiv.add(resultPassif), fontNormalBold, colorBorder));

            document.add(tablePassif);

            // Nouvelle Page
            document.newPage();

            // ==========================================
            // PAGE 3 : ANNEXE 2033-B (COMPTE DE RÉSULTAT)
            // ==========================================
            genererEnTeteCerfa(document, "N° 2033-B-SD", "15948*08", "COMPTE DE RÉSULTAT SIMPLIFIÉ", annee, fontTitre, fontSousTitre, fontMini);

            document.add(new Paragraph("A - RÉSULTAT COMPTABLE", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableResult = new PdfPTable(3);
            tableResult.setWidthPercentage(100);
            tableResult.setWidths(new float[]{60, 15, 25});
            tableResult.setSpacingAfter(15);

            tableResult.addCell(creerCelluleHeader("Produits / Charges d'exploitation", fontNormalBold, colorHeader));
            tableResult.addCell(creerCelluleHeader("Code", fontNormalBold, colorHeader));
            tableResult.addCell(creerCelluleHeader("Montant N", fontNormalBold, colorHeader));

            // Ventes & Services
            tableResult.addCell(creerCellule("Prestations de services (Chiffre d'affaires LMNP)", fontNormal, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("217", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(bilan.recettesBrutes(), fontNormal, colorBorder));

            tableResult.addCell(creerCellule("Total des produits d'exploitation (I)", fontNormalBold, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("232", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(bilan.recettesBrutes(), fontNormalBold, colorBorder));

            // Charges
            tableResult.addCell(creerCellule("Achats d'eau, énergie et fournitures", fontNormal, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("238", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(totalAchats, fontNormal, colorBorder));

            tableResult.addCell(creerCellule("Autres charges externes (Assurances, entretien, frais plateformes, etc.)", fontNormal, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("242", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(totalAutresChargesExternes, fontNormal, colorBorder));

            tableResult.addCell(creerCellule("Impôts, taxes et versements assimilés (CFE, etc.)", fontNormal, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("244", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(totalImpotsTaxes, fontNormal, colorBorder));

            tableResult.addCell(creerCellule("Dotations aux amortissements de l'exercice", fontNormal, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("254", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(bilan.amortissementUtilise(), fontNormal, colorBorder));

            tableResult.addCell(creerCellule("Total des charges d'exploitation (II)", fontNormalBold, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("264", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(totalAchats.add(totalAutresChargesExternes).add(totalImpotsTaxes).add(bilan.amortissementUtilise()), fontNormalBold, colorBorder));

            // Résultat d'exploitation (I - II)
            BigDecimal resultExploit = bilan.recettesBrutes().subtract(totalAchats.add(totalAutresChargesExternes).add(totalImpotsTaxes).add(bilan.amortissementUtilise()));
            tableResult.addCell(creerCellule("1 - RÉSULTAT D'EXPLOITATION (Bénéfice ou perte)", fontNormalBold, false, colorBorder));
            tableResult.addCell(creerCelluleCodeCase("270", fontCodeCase, colorBorder));
            tableResult.addCell(creerCelluleMontant(resultExploit, fontNormalBold, colorBorder));

            document.add(tableResult);

            // B - RÉSULTAT FISCAL
            document.add(new Paragraph("B - RÉSULTAT FISCAL", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableResultFiscal = new PdfPTable(3);
            tableResultFiscal.setWidthPercentage(100);
            tableResultFiscal.setWidths(new float[]{60, 15, 25});
            tableResultFiscal.setSpacingAfter(15);

            tableResultFiscal.addCell(creerCelluleHeader("Réintégrations / Déductions", fontNormalBold, colorHeader));
            tableResultFiscal.addCell(creerCelluleHeader("Code", fontNormalBold, colorHeader));
            tableResultFiscal.addCell(creerCelluleHeader("Montant", fontNormalBold, colorHeader));

            tableResultFiscal.addCell(creerCellule("Bénéfice comptable (si positif)", fontNormal, false, colorBorder));
            tableResultFiscal.addCell(creerCelluleCodeCase("312", fontCodeCase, colorBorder));
            tableResultFiscal.addCell(creerCelluleMontant(resultExploit.compareTo(BigDecimal.ZERO) > 0 ? resultExploit : BigDecimal.ZERO, fontNormal, colorBorder));

            tableResultFiscal.addCell(creerCellule("Déficit comptable (si négatif)", fontNormal, false, colorBorder));
            tableResultFiscal.addCell(creerCelluleCodeCase("314", fontCodeCase, colorBorder));
            tableResultFiscal.addCell(creerCelluleMontant(resultExploit.compareTo(BigDecimal.ZERO) < 0 ? resultExploit.abs() : BigDecimal.ZERO, fontNormal, colorBorder));

            // Déduction amortissement
            tableResultFiscal.addCell(creerCellule("Déduction : Amortissements utilisés de l'exercice", fontNormal, false, colorBorder));
            tableResultFiscal.addCell(creerCelluleCodeCase("350", fontCodeCase, colorBorder));
            tableResultFiscal.addCell(creerCelluleMontant(bilan.amortissementUtilise(), fontNormal, colorBorder));

            tableResultFiscal.addCell(creerCellule("RÉSULTAT FISCAL (Bénéfice imposable BIC)", fontNormalBold, false, colorBorder));
            tableResultFiscal.addCell(creerCelluleCodeCase("352 / 354", fontCodeCase, colorBorder));
            tableResultFiscal.addCell(creerCelluleMontant(beneficeReel, fontNormalBold, colorBorder));

            document.add(tableResultFiscal);

            // Nouvelle Page
            document.newPage();

            // ==========================================
            // PAGE 4 : ANNEXE 2033-C (IMMOBILISATIONS & AMORTISSEMENTS)
            // ==========================================
            genererEnTeteCerfa(document, "N° 2033-C-SD", "15948*08", "IMMOBILISATIONS & AMORTISSEMENTS DETACHÉS", annee, fontTitre, fontSousTitre, fontMini);

            document.add(new Paragraph("I - IMMOBILISATIONS ET ACTIFS", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableImmos = new PdfPTable(5);
            tableImmos.setWidthPercentage(100);
            tableImmos.setWidths(new float[]{40, 15, 15, 15, 15});
            tableImmos.setSpacingAfter(15);

            tableImmos.addCell(creerCelluleHeader("Catégorie d'immobilisation", fontNormalBold, colorHeader));
            tableImmos.addCell(creerCelluleHeader("Valeur début d'ex.", fontNormalBold, colorHeader));
            tableImmos.addCell(creerCelluleHeader("Augmentations", fontNormalBold, colorHeader));
            tableImmos.addCell(creerCelluleHeader("Diminutions", fontNormalBold, colorHeader));
            tableImmos.addCell(creerCelluleHeader("Valeur fin d'ex.", fontNormalBold, colorHeader));

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

            tableImmos.addCell(creerCellule("Bâtiments et Constructions (Tiny House)", fontNormal, false, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoDebut, fontNormal, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoAug, fontNormal, colorBorder));
            tableImmos.addCell(creerCelluleMontant(BigDecimal.ZERO, fontNormal, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoDebut.add(immoAug), fontNormalBold, colorBorder));

            tableImmos.addCell(creerCellule("TOTAL GÉNÉRAL", fontNormalBold, false, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoDebut, fontNormalBold, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoAug, fontNormalBold, colorBorder));
            tableImmos.addCell(creerCelluleMontant(BigDecimal.ZERO, fontNormalBold, colorBorder));
            tableImmos.addCell(creerCelluleMontant(immoDebut.add(immoAug), fontNormalBold, colorBorder));

            document.add(tableImmos);

            document.add(new Paragraph("II - AMORTISSEMENTS", fontSousTitre));
            document.add(Chunk.NEWLINE);

            PdfPTable tableAmortTab = new PdfPTable(5);
            tableAmortTab.setWidthPercentage(100);
            tableAmortTab.setWidths(new float[]{40, 15, 15, 15, 15});
            tableAmortTab.setSpacingAfter(15);

            tableAmortTab.addCell(creerCelluleHeader("Éléments amortissables", fontNormalBold, colorHeader));
            tableAmortTab.addCell(creerCelluleHeader("Cumul début d'ex.", fontNormalBold, colorHeader));
            tableAmortTab.addCell(creerCelluleHeader("Dotation de l'ex.", fontNormalBold, colorHeader));
            tableAmortTab.addCell(creerCelluleHeader("Reprises", fontNormalBold, colorHeader));
            tableAmortTab.addCell(creerCelluleHeader("Cumul fin d'ex.", fontNormalBold, colorHeader));

            BigDecimal amortDebut = BigDecimal.ZERO;
            BigDecimal amortDot = bilan.amortissementUtilise();

            for (BienAmortissable b : biens) {
                LocalDate dateMiseEnService = b.getDateMiseEnService() != null ? b.getDateMiseEnService() : b.getDateAchat();
                if (dateMiseEnService == null) continue;
                int duree = b.getDureeAmortissementAns() != null ? b.getDureeAmortissementAns() : 1;
                BigDecimal annuel = b.getAmortissementAnnuel() != null ? b.getAmortissementAnnuel() : BigDecimal.ZERO;

                // Amortissement cumulé avant cet exercice
                int anneesAmortiesAvant = Math.max(0, Math.min(duree, annee - dateMiseEnService.getYear()));
                amortDebut = amortDebut.add(annuel.multiply(BigDecimal.valueOf(anneesAmortiesAvant)));
            }

            tableAmortTab.addCell(creerCellule("Amortissement des constructions (Tiny)", fontNormal, false, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDebut, fontNormal, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDot, fontNormal, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(BigDecimal.ZERO, fontNormal, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDebut.add(amortDot), fontNormalBold, colorBorder));

            tableAmortTab.addCell(creerCellule("TOTAL GÉNÉRAL", fontNormalBold, false, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDebut, fontNormalBold, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDot, fontNormalBold, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(BigDecimal.ZERO, fontNormalBold, colorBorder));
            tableAmortTab.addCell(creerCelluleMontant(amortDebut.add(amortDot), fontNormalBold, colorBorder));

            document.add(tableAmortTab);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    private void genererEnTeteCerfa(Document document, String cerfaNumero, String cerfaSuf, String titreDoc, Integer annee, Font fontTitre, Font fontSousTitre, Font fontMini) throws DocumentException {
        PdfPTable tableHeader = new PdfPTable(3);
        tableHeader.setWidthPercentage(100);
        tableHeader.setWidths(new float[]{30, 45, 25});
        tableHeader.setSpacingAfter(20);

        // République Française
        PdfPCell cell1 = new PdfPCell(new Paragraph("RÉPUBLIQUE\nFRANÇAISE\n\nLiberté\nÉgalité\nFraternité", fontMini));
        cell1.setBorder(Rectangle.NO_BORDER);
        tableHeader.addCell(cell1);

        // Titre central
        PdfPCell cell2 = new PdfPCell(new Paragraph(titreDoc + "\nExercice fiscal " + annee, fontTitre));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setBorder(Rectangle.NO_BORDER);
        tableHeader.addCell(cell2);

        // Cerfa à droite
        PdfPCell cell3 = new PdfPCell(new Paragraph(cerfaNumero + "\nFormulaire officiel\nN° " + cerfaSuf, fontMini));
        cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell3.setBorder(Rectangle.NO_BORDER);
        tableHeader.addCell(cell3);

        document.add(tableHeader);

        // Séparateur
        Paragraph pSep = new Paragraph("__________________________________________________________________________________________", fontMini);
        pSep.setSpacingAfter(15);
        document.add(pSep);
    }

    private PdfPCell creerCelluleHeader(String texte, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(texte, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new Color(30, 41, 59)); // Slate 800
        return cell;
    }

    private PdfPCell creerCellule(String texte, Font font, boolean center, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(texte, font));
        cell.setPadding(6);
        if (center) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        cell.setBorderColor(borderColor);
        return cell;
    }

    private PdfPCell creerCelluleCodeCase(String code, Font font, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(code, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(borderColor);
        cell.setBackgroundColor(new Color(241, 245, 249)); // Slate 100
        return cell;
    }

    private PdfPCell creerCelluleMontant(BigDecimal montant, Font font, Color borderColor) {
        String valText = montant != null ? formatEuro(montant) : "0,00 €";
        PdfPCell cell = new PdfPCell(new Paragraph(valText, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(borderColor);
        return cell;
    }

    private String formatEuro(BigDecimal val) {
        if (val == null) return "0,00 €";
        return String.format("%,.2f €", val.setScale(2, RoundingMode.HALF_UP));
    }
}
