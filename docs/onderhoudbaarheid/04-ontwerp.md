# 4 — Aangepast ontwerp E1

| | |
|---|---|
| **Verbetering** | E1 — Extract Constant in `HibernateAppointmentDAO.java` |
| **Geselecteerd in** | [`03-prioritering.md`](./03-prioritering.md) §5 (score 30, hoogste van 4 kandidaten) |
| **Sonar-rule** | `java:S1192` — *"Define a constant instead of duplicating this literal"* |
| **Scope** | Ontwerp; de realisatie volgt in bp5 |

---

## 1. Doel

Beschrijven *hoe* we het probleem uit bp3 willen oplossen, welke alternatieven
zijn afgewogen en op welke ontwerpprincipes en refactoring-patronen de keuze
rust.

## 2. Probleemstelling

`HibernateAppointmentDAO.java` bevat vier Hibernate-property-namen die elk
3 tot 4 keer als string-literal herhaald worden:

| Literal | Aantal | Sonar-impact |
|---|---|---|
| `"patient"` | 4× | HIGH-MAINTAINABILITY |
| `"status"` | 3× | HIGH-MAINTAINABILITY |
| `"voided"` | 3× | HIGH-MAINTAINABILITY |
| `"timeSlot"` | 3× | HIGH-MAINTAINABILITY |

Een typefout in één van de 4 `"patient"`-literals breekt één codepad terwijl de
andere drie blijven werken — een bug die alleen via een runtime-test op precies
dat pad zichtbaar wordt. Een rename van de Hibernate-mapping vereist een
file-wide search-and-replace; elke gemiste plek = stille runtime-fout.

## 3. Alternatieven

Drie reële opties tegen elkaar afgezet.

| # | Optie | Voor | Tegen | Past? |
|---|---|---|---|:--:|
| A | `private static final String` per literal in **dezelfde klasse** | Minimale diff, geen API-impact, bytecode-equivalent na compile | Constanten blijven privé; niet herbruikbaar in andere DAO's | ✅ |
| B | `public static final String` op een **nieuwe `AppointmentDaoConstants`-klasse** | Hergebruik over meerdere DAO's, centrale plek | Premature abstraction — geen andere file gebruikt deze literals nu (YAGNI); introduceert nieuwe public API | ❌ |
| C | `enum AppointmentProperty { PATIENT("patient"), STATUS("status"), … }` | Type-safe; uitbreidbaar | Overkill voor losse property-namen; verbergt dat het Hibernate-property-strings zijn | ❌ |

## 4. Gekozen ontwerp — optie A

Vier `private static final String`-velden bovenin de klasse, daarna alle
duplicate literals door de constantes vervangen.

**Vóór** (huidige code):
```java
public class HibernateAppointmentDAO extends HibernateSingleClassDAO
        implements AppointmentDAO {

    public HibernateAppointmentDAO() { super(Appointment.class); }

    // ...
    criteria.add(Restrictions.eq("patient", patient));
    criteria.add(Restrictions.eq("voided", false));
}
```

**Na** (ontwerp-doel):
```java
public class HibernateAppointmentDAO extends HibernateSingleClassDAO
        implements AppointmentDAO {

    private static final String PATIENT    = "patient";
    private static final String STATUS     = "status";
    private static final String VOIDED     = "voided";
    private static final String TIME_SLOT  = "timeSlot";

    public HibernateAppointmentDAO() { super(Appointment.class); }

    // ...
    criteria.add(Restrictions.eq(PATIENT, patient));
    criteria.add(Restrictions.eq(VOIDED, false));
}
```

Detailkeuzes: `private` (YAGNI — geen consumer buiten deze klasse),
`SCREAMING_SNAKE_CASE` (Java-conventie JLS §6.1), naamgeving = property-naam
in hoofdletters.

## 5. Onderbouwing — principes en patronen

| Element | Soort | Toelichting |
|---|---|---|
| **DRY — Don't Repeat Yourself** (Hunt & Thomas, *The Pragmatic Programmer*, 1999) | Ontwerpprincipe | Eén plek waar `"patient"` staat. Toekomstige rename = 1 wijziging i.p.v. 4. |
| **YAGNI — You Aren't Gonna Need It** (Beck, *Extreme Programming Explained*, 1999) | Ontwerpprincipe | Géén `enum` of separate Constants-klasse — alleen wat de huidige Sonar-issue daadwerkelijk vraagt. Onderbouwt waarom alternatief B en C afvallen. |
| **Extract Constant** (Fowler, *Refactoring* 2e ed., 2018, hfdst 9) | Refactoring-patroon | Canonieke catalog-refactor: vervang een magische waarde door een named constant. Pre/post-gedrag identiek. |

## 6. Motivatie op kwaliteitseisen

| Eis | Bron | Stand vóór E1 | Stand verwacht na E1 |
|---|---|---|:--:|
| MNT-2 — Duplicaten ≤ 5 % | bp1 | 1,6 % | 1,6 % (geen verslechtering; potentieel lichte daling) |
| REL-1 — 0 falende tests | bp2 | 182/182 ✅ | 182/182 ✅ (bytecode-equivalent → geen regressie) |
| Sonar-rule `java:S1192` op deze file | bp1 export | 4 hits | **0 hits** (kerndoel van E1) |
| Sonar-totaal `total` | bp1 export | 426 | **422** (machineel verifieerbaar) |

## 7. Vervolg

Dit ontwerp wordt gerealiseerd in **bp5** (PoC) en gevalideerd in **bp6**
(Sonar-rerun + 182/182 tests + PIT 15/15).
