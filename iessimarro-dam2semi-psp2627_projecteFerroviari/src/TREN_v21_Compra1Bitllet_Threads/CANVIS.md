# Canvis de la v2.1 respecte a la v1.1

| | Versió anterior | Aquesta versió |
|---|---|---|
| **Versió** | v1.1: compra d'1 bitllet sense fils | v2.1: compra d'1 bitllet amb fils (**solució del Treball 2**) |
| **Paquet** | [`TREN_v11_Compra1Bitllet`](../TREN_v11_Compra1Bitllet) | `TREN_v21_Compra1Bitllet_Threads` |
| **Etiqueta** | [`v1.1`](../../../../../releases/tag/v1.1) | [`v2.1`](../../../../../releases/tag/v2.1) |

[Tornar al README](../../../README.md)

## 1. Què es demanava

Partint del codi del Treball 1, la compra d'un bitllet (opció 14 del menú) s'ha d'executar en un fil independent del fil principal (`filCompraBitlletTrajecte`). La resta de la gestió de la companyia la continua fent el fil principal. Els requisits de l'enunciat són:

1. L'entrada de teclat és comuna a tots els fils: cada pregunta porta com a prefix el fil que la fa (p.e. `[fil ppal]`).
2. Cal **sincronitzar l'accés al teclat** perquè un fil no capture dades destinades a un altre.
3. El fil principal pot esperar que acabe la compra (`join()`), però cal preveure diversos fils de compra concurrents i **sincronitzar l'accés als seients**.
4. En triar l'opció 14 s'han de llançar **almenys 2 fils** per a poder provar la competència per un mateix seient.

## 2. Conceptes destacats

- Creació de fils amb la interfície `Runnable`, nom dels fils (`setName()`, `getName()`) i espera amb `join()`.
- **Recursos compartits:** el teclat i els seients d'un vagó.
- **Monitors:** mètodes `synchronized` d'instància (monitor = l'objecte `Vago`), mètodes `static synchronized` (monitor = `Validacio.class`) i blocs `synchronized (Validacio.class) { ... }`.
- **Operació atòmica:** comprovar i reservar un seient en un sol pas.
- **Construcció de cadenes amb StringBuilder**: quan diversos fils poden escriure a la consola alhora, una eixida feta amb molts print seguits es pot barrejar amb la d’altres fils. Amb `StringBuilder` el text sencer es construeix primer en memòria i s’imprimeix amb un únic println(sb), de manera que ix d’una sola vegada, sense que s’hi intercale l’eixida d’altres fils. A més, és més eficient que concatenar String dins d’un bucle.
- **Reentrada** dels monitors de Java i **ordre dels bloquejos** per a evitar l'interbloqueig.
- `Thread.sleep()` fora de qualsevol bloqueig: els pagaments de diversos fils es fan en paral·lel.

## 3. Resum dels canvis

| Fitxer | Canvi |
|---|---|
| Tots | paquet `TREN_v21_Compra1Bitllet_Threads` (abans `TREN_v11_Compra1Bitllet`). |
| `Categoria`, `Seient`, `Trajecte`, `Tren` | Sense canvis (només el nom del paquet i un comentari `[v2.1]` que ho indica). |
| `CompanyiaFerroviaria` | Nom del fil principal, opció 14 amb fils (`llancaFilsCompra()`), prefix del fil a `pagamentBitllet()`; `compraBitlletTrajecte()` passa al fil. |
| `Validacio` | Teclat sincronitzat i prefix amb el nom del fil. |
| `Vago` | Mètodes `synchronized` per als seients i eixida sense barrejar. |
| `FilCompraBitlletTrajecte` | **Nou.** Compra d'un bitllet en 4 fases. |

Dins del codi, cada canvi està marcat amb un comentari `[v2.1]`. La capçalera de `CompanyiaFerroviaria` en conté el resum.

## 4. `CompanyiaFerroviaria`

- **Nom del fil principal.** Al `main()` es posa `Thread.currentThread().setName("fil ppal")`. El nom ix com a prefix a cada pregunta.
- **Atributs nous:**
  - `NUM_FILS_COMPRA = 2`: el requisit 4 demana almenys 2 fils;
  - `numFil`: comptador per a numerar els fils.
- **Opció 14.** Crida `llancaFilsCompra()`, un mètode nou que:
  - crea els fils `filCompraBitlletTrajecte1`, `filCompraBitlletTrajecte2`…;
  - els llança amb `start()`;
  - espera que acaben amb `join()`, tal com permet el requisit 3.
- **`pagamentBitllet(BigDecimal)` es manté com a mètode propi**, igual que a la v1.1, i el criden els fils:
  - els missatges porten el prefix del fil;
  - el `Thread.sleep(3000)` es fa **sense cap bloqueig**, així que els pagaments de diversos fils es fan en paral·lel: si dos fils paguen, tarden uns 3 s en total, no 6;
  - només la confirmació S/N ocupa el teclat, el temps de llegir la resposta.
- **`compraBitlletTrajecte()`** es trasllada a `FilCompraBitlletTrajecte`.

## 5. `Validacio`: el teclat és un recurs compartit

- **Monitor del teclat.** Tots els mètodes públics `valida...` són `public static synchronized`, i el monitor és `Validacio.class`. Així, mai no llegeixen dos fils alhora.
- **Mètode nou `prefix()`.** Retorna `"[" + nom del fil + "] "`.
  - `llegeixLinia()` el posa davant de cada pregunta, després dels salts de línia inicials.
  - El mètode nou `error()` el posa davant dels missatges d'error de `Validacio`.
- **Preguntes agrupades.** Un fil pot fer diverses preguntes seguides dins de `synchronized (Validacio.class) { ... }` sense que s'hi fique cap altre fil. Funciona perquè el fil que ja té el monitor de `Validacio.class` pot tornar a entrar als mètodes `static synchronized` de `Validacio` (els monitors de Java són **reentrants**).

## 6. `Vago`: els seients són un recurs compartit

El monitor és **cada objecte `Vago`**, i dins d'aquests mètodes no es llig mai el teclat.

- **Mètodes nous:**
  - `synchronized boolean reservaSeient(Seient s)`: **comprova i reserva en una sola operació atòmica**, i retorna `false` si el seient ja no està lliure. Si es feren en dos passos, dos fils podrien veure el seient lliure i reservar-lo tots dos.
  - `synchronized void ocupaSeient(Seient s)`.
  - `synchronized void alliberaSeient(Seient s)`.
- **Mètodes que passen a ser `synchronized`:** `mapaVago()`, `numSeientsLliures()` i `teReserves()`.
- **Eixida sense barrejar:**
  - `mapaVago()` construeix el text amb un `StringBuilder` per evitar que s’hi barreje l’eixida d’altres fils i l'imprimeix d'una sola vegada;
  - `imprimirBitllet()` construeix el bitllet sencer, amb el prefix del fil, i també l'imprimeix d'una vegada.

## 7. `FilCompraBitlletTrajecte` (nou)

Classe que implementa `Runnable` i conté el codi de compra de la v1.1, amb els mateixos passos 1-10, però organitzat en fases. Els missatges porten el prefix del fil (mètode `missatge()`) i la reserva del seient es fa amb `vago.reservaSeient(triat)` en lloc de `triat.verificaSeient()` + `seient.reservaSeient()`.

| Fase | Accions | Bloqueig |
|---|---|---|
| 1 | Passos 1-5: tria del trajecte, la categoria i el vagó, i mapa del vagó | `synchronized (Validacio.class)`: el fil manté el teclat tota la fase (i pren el `Vago` un moment per a comptar els seients lliures i fer el mapa). |
| 2 | Pas 6: tria del seient i `vago.reservaSeient(triat)` | Teclat i, dins, el `Vago`. |
| 3 | Pagament: `CompanyiaFerroviaria.pagamentBitllet(preu)` | Cap durant el retard; el teclat només per a la confirmació S/N. |
| 4 | Passos 7-10: ocupar o alliberar el seient, imprimir el bitllet i mostrar el mapa | `Vago` (ocupar, alliberar i mapa). |

**Ordre dels bloquejos:** sempre teclat → vagó, mai a l'inrevés. Per això no hi pot haver interbloqueig.

## 8. Proves de validació

Totes les proves es fan amb les dades inicials (`carregaDadesInicials()`) i l'opció 14, que llança 2 fils. En tots els casos els fils trien VLC-MAD, Turista i `vago02`.

| Prova | Resultat esperat |
|---|---|
| **Competència pel mateix seient:** el primer fil reserva el 01A i el segon també tria el 01A | El segon fil rep «no està lliure (RESERVAT)» o «(OCUPAT)» i torna a demanar el seient. **Cap seient es ven dues vegades.** |
| **Pagament en paral·lel:** cada fil reserva un seient diferent (01A i 01B) i tots dos paguen | Es fan els dos pagaments alhora: la compra tarda uns 3 s, no 6. |
| **Pagament rebutjat:** un fil respon N a la confirmació | El seu seient torna a quedar lliure i l'altre fil acaba la compra normalment. |
| **Cancel·lació:** un fil introduïx 0 en qualsevol pas | Aquest fil acaba sense reservar res i l'altre continua la compra. |
| **Prefix del fil:** després de la compra, crear un vagó des del menú | Les preguntes porten el prefix `[fil ppal]` i el menú continua funcionant. |
| **Mapa final** | Cap seient queda en estat *reservat* (`?`) quan els dos fils han acabat. |

[Tornar al README](../../../README.md)
