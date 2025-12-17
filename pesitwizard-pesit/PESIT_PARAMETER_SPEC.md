# PESIT Parameter Specification

## Summary of Changes

The `ParameterIdentifier` and `ParameterGroupIdentifier` enums have been corrected based on the official PESIT E specification (pesit.md).

## ParameterIdentifier (PI) - Complete List from Spec

Based on § 4.7.2.2 of the PESIT specification:

| Code | Enum Name | Description (French) | Description (English) |
|------|-----------|----------------------|----------------------|
| 1 | PI_01_CRC | Utilisation d'un CRC | CRC Usage |
| 2 | PI_02_DIAG | Diagnostic | Diagnostic |
| 3 | PI_03_DEMANDEUR | Identification Demandeur | Requestor Identification |
| 4 | PI_04_SERVEUR | Identification Serveur | Server Identification |
| 5 | PI_05_CONTROLE_ACCES | Contrôle d'accès | Access Control |
| 6 | PI_06_VERSION | Numéro de version | Version Number |
| 7 | PI_07_SYNC_POINTS | Option points de synchronisation | Sync Points Option |
| 11 | PI_11_TYPE_FICHIER | Type du fichier | File Type |
| 12 | PI_12_NOM_FICHIER | Nom du fichier | Filename |
| 13 | PI_13_ID_TRANSFERT | Identificateur du transfert | Transfer Identifier |
| 14 | PI_14_ATTRIBUTS_DEMANDES | Attributs demandés | Requested Attributes |
| 15 | PI_15_TRANSFERT_RELANCE | Transfert relancé | Transfer Restarted |
| 16 | PI_16_CODE_DONNEES | Code-données | Data Code |
| 17 | PI_17_PRIORITE | Priorité de transfert | Transfer Priority |
| 18 | PI_18_POINT_RELANCE | Point de relance | Restart Point |
| 19 | PI_19_CODE_FICHIER | Code fichier | File Code |
| 20 | PI_20_NUM_SYNC | Numéro du point de synchronisation | Sync Point Number |
| 21 | PI_21_COMPRESSION | Compression | Compression |
| 22 | PI_22_TYPE_ACCES | Type d'accès | Access Type |
| 23 | PI_23_RESYNC | Resynchronisation | Resynchronization |
| 25 | PI_25_TAILLE_MAX_ENTITE | Taille maximale d'une entité de données | Max Data Entity Size |
| 26 | PI_26_TIMEOUT | Temporisation de surveillance | Timeout |
| 27 | PI_27_NB_OCTETS | Nombre d'octets de données | Data Byte Count |
| 28 | PI_28_NB_ARTICLES | Nombre d'articles | Article Count |
| 29 | PI_29_COMPLEMENT_DIAG | Compléments de diagnostic | Diagnostic Complement |
| 31 | PI_31_FORMAT_ARTICLE | Format d'article | Article Format |
| 32 | PI_32_LONG_ARTICLE | Longueur d'article | Article Length |
| 33 | PI_33_ORG_FICHIER | Organisation du fichier | File Organization |
| 34 | PI_34_SIGNATURE | Prise en compte de la signature | Signature Handling |
| 36 | PI_36_SCEAU_SIT | Sceau SIT | SIT Seal |
| 37 | PI_37_LABEL_FICHIER | Label du fichier | File Label |
| 38 | PI_38_LONG_CLE | Longueur de la clé | Key Length |
| 39 | PI_39_DEPL_CLE | Déplacement de la clé | Key Offset in Record |
| 41 | PI_41_UNITE_RESERVATION | Unité de réservation | Reservation Unit |
| 42 | PI_42_MAX_RESERVATION | Valeur maximale de réservation d'espace | Max Space Reservation |
| 51 | PI_51_DATE_CREATION | Date et heure de création | Creation Date/Time |
| 52 | PI_52_DATE_EXTRACTION | Date et heure de dernière extraction | Last Extraction Date/Time |
| 61 | PI_61_ID_CLIENT | Identificateur Client | Client Identifier |
| 62 | PI_62_ID_BANQUE | Identificateur Banque | Bank Identifier |
| 63 | PI_63_ACCES_FICHIER | Contrôle d'accès fichier | File Access Control |
| 64 | PI_64_DATE_SERVEUR | Date et heure du serveur | Server Date/Time |
| 71 | PI_71_TYPE_AUTH | Type d'authentification | Authentication Type |
| 72 | PI_72_ELEMS_AUTH | Eléments d'authentification | Authentication Elements |
| 73 | PI_73_TYPE_SCELLEMENT | Type de scellement | Sealing Type |
| 74 | PI_74_ELEMS_SCELLEMENT | Eléments de scellement | Sealing Elements |
| 75 | PI_75_TYPE_CHIFFR | Type de chiffrement | Encryption Type |
| 76 | PI_76_ELEMS_CHIFFR | Eléments de chiffrement | Encryption Elements |
| 77 | PI_77_TYPE_SIG | Type de signature | Signature Type |
| 78 | PI_78_SCEAU | Sceau | Seal |
| 79 | PI_79_SIGNATURE | Signature | Signature |
| 80 | PI_80_ACCREDITATION | Accréditation | Accreditation |
| 81 | PI_81_ACCUSE_SIG | Accusé de réception de la signature | Signature Receipt Acknowledgment |
| 82 | PI_82_DEUTURE | Deuture | Deuture |
| 83 | PI_83_ACCRED_2 | Deuxième accréditation | Second Accreditation |
| 91 | PI_91_MESSAGE | Message | Message |
| 99 | PI_99_MESSAGE_LIBRE | Message libre | Free Message |

## ParameterGroupIdentifier (PGI) - Complete List from Spec

Based on § 4.7.2.2 of the PESIT specification:

| Code | Enum Name | Description | Contains PIs |
|------|-----------|-------------|--------------|
| 9 | PGI_09_ID_FICHIER | Identificateur de fichier (File Identifier) | PI 3, 4, 11, 12 |
| 30 | PGI_30_ATTR_LOGIQUES | Attributs logiques (Logical Attributes) | PI 31, 32, 33, 34, 36, 37, 38, 39 |
| 40 | PGI_40_ATTR_PHYSIQUES | Attributs physiques (Physical Attributes) | PI 41, 42 |
| 50 | PGI_50_ATTR_HISTORIQUES | Attributs historiques (Historical Attributes) | PI 51, 52 |

## Key Differences from Previous Implementation

### Parameter Identifier Changes
- **Removed** invented parameters like `PI_10_ID_FICH`, `PI_24_UTILISATEUR`, `PI_25_MOT_PASSE`, `PI_26_PROFILE`, `PI_30_DATE_FICH`, `PI_40_VERSION_PROTOCOLE`, `PI_50_ACCES`, `PI_60_CLASSE_APPLI`
- **Renamed** many PIs to match the spec (e.g., `PI_11_NOM_FICH_LOCAL` → `PI_11_TYPE_FICHIER`)
- **Fixed codes** - PI 13 is Transfer ID (was incorrectly mapped to Final Filename)
- **Added missing PIs** like PI 1 (CRC), PI 61-64 (Client/Bank IDs), PI 71-83 (Security parameters), PI 91, PI 99

### Parameter Group Identifier Changes
- **Fixed codes**: The spec defines PGI 9, 30, 40, 50 (not 0x10, 0x20, 0x30, 0x40)
- **PGI 9**: File Identifier - contains PI 3 (Requestor), PI 4 (Server), PI 11 (File Type), PI 12 (Filename)
- **PGI 30**: Logical Attributes - file structure parameters
- **PGI 40**: Physical Attributes - space reservation
- **PGI 50**: Historical Attributes - dates

### Impact on Code
All code using the old parameter names will need to be updated to use the correct PESIT specification names and codes.

## Reference
Source: `pesit.md` § 4.7.2.2 "Liste des codes PGI et PI"
