# Projecte FERROVIARI (RENFE) – Compra de bitllets

Repositori de Github del mòdul PSP de l'IES LLUIS SIMARRO específic per al projecte FERROVIARI

<a name="top"></a>

## Índex de continguts
* [Esquelet (v1.0)](#v10)
* [Projecte Ferroviari versió 1 bitllet sense fils (v1.1)](#v11)



### Contingut del Repositori del projecte FERROVIARI:
| Data | Etiqueta | Descripcio|
| ---- | ---- | ---- |
| 31/09/2026 | [v1.0](#v10) | Esquelet inicial del projecte **TREN_v10_Esquelet**|
| 19/11/2026 | [v1.1](#v11) · [release](../../releases/tag/v1.1) | Solució del Treball 1: compra d'1 bitllet sense fils **TREN_v11_Compra1Bitllet** | [`TREN_v11_Compra1Bitllet`](TREN_v11_Compra1Bitllet) · [`CANVIS.md`](TRENv11_Compra1Bitllet/CANVIS.md) |

<a name="v10"></a>
## Projecte Ferroviari (v1.0)
### Introducció

Aplicació Java de consola per a la gestió ferroviària de trajectes de trens de llarga distància de la companyia **RENFE**, amb l'objectiu final de **comprar un bitllet** en una categoria concreta d'un tren que cobreix un trajecte entre dues ciutats, en un dia i hora determinats.

Material creat per **Enrique Niclós Camarasa**, per al mòdul *Programació de serveis i processos*.

> Aquest repositori conserva **diverses versions** del projecte, cadascuna en el seu propi paquet. Totes comparteixen el model de domini i les regles de negoci descrites ací.

---

### Característiques principals

- Aplicació **Java de consola**: no hi ha interfície gràfica, totes les dades s'introduïxen per teclat.
- Gestió completa (**crear, modificar i eliminar**) de Vagons, Categories, Trens i Trajectes mitjançant un menú.
- **Compra d'un únic bitllet** per operació, seguint un flux guiat: Trajecte → Categoria → Vagó → Seient.
- Mapa visual del vagó amb l'estat de cada seient.
- Simulació del **pagament** i impressió del bitllet.
- Validació de totes les dades d'entrada i control de la **capacitat dels trens**.
- Càrrega de **dades inicials** per a poder provar l'aplicació sense crear-ho tot des de zero.
- Es pot cancel·lar la majoria d'accions introduint `0` en lloc de l'identificador.
- En els mètodes interactius de modificació, prémer **INTRO** manté el valor anterior.

---

### Model de domini

```
CompanyiaFerroviaria
 ├── Tren (idTren, capacitat)
 └── Trajecte (origen, destí, dia/hora, duració, tren)
      └── Categoria (tipus, descripció, preu)
           └── Vagó (nPlaces)
                └── Seient (fila + lletra, estat)
```

| Classe | Responsabilitat |
|---|---|
| **Seient** | Identificador (p.e. `07A`) i estat: `LLIURE` (`O`), `OCUPAT` (`X`) o `RESERVAT` (`?`). |
| **Vago** | Conjunt de seients en fileres de 4 (`A`, `B`, `C`, `D`); mapa del vagó i impressió del bitllet. |
| **Categoria** | Tipus (`Turista`, `TuristaPlus`, `Preferent`), preu del seient i llista de vagons. |
| **Trajecte** | Origen, destí, horari, duració, tren assignat i categories ofertes. |
| **Tren** | Identificador i capacitat màxima de places. |
| **CompanyiaFerroviaria** | Classe principal: llistes de trens, trajectes, categories i vagons, menú i compra. |
| **Validacio** | Classe auxiliar estàtica de validació d'entrada per teclat (un únic `Scanner` compartit). |

---

### Regles de negoci

- Un **Trajecte** està cobert per **un únic Tren** i ofereix una o més **Categories**. Sense tren assignat no es poden afegir categories ni vendre bitllets.
- Una **Categoria** inclou un o més **Vagons** complets i fixa el **preu** dels seients.
- Cada **Vagó** pertany com a màxim a **una Categoria**, i cada **Categoria** a un únic **Trajecte**. Els no assignats es consideren *lliures*.
- Els seients pertanyen al Vagó; el seu nombre ha de ser **múltiple de 4** (màxim 100 per vagó).
- La suma de places de tots els vagons d'un trajecte **no pot superar la capacitat del Tren**. Es comprova en afegir categories o vagons, en modificar les places d'un vagó, en canviar el tren d'un trajecte i en reduir la capacitat d'un tren.
- **No es pot esborrar** un objecte en ús (vagó assignat, categoria assignada, tren que cobreix un trajecte o trajecte amb seients reservats o ocupats).
- **No es pot llevar ni modificar** un vagó, categoria o trajecte amb seients reservats o ocupats.
- Sobre un seient ocupat no es permet cap acció.
- Els identificadors han de ser únics dins de cada tipus d'objecte.

#### Distribució dels seients

```
|  [1A] [1B]      [1C] [1D]  |
|  [2A] [2B]      [2C] [2D]  |
|   …    …         …    …    |
|  [nA] [nB]      [nC] [nD]  |
```

`A` i `D` són seients de finestra; `B` i `C`, de corredor.

---

### Procés de compra

Per a poder comprar cal que existisca el trajecte, que tinga tren assignat, almenys una categoria, que la categoria tinga almenys un vagó i que el vagó tinga almenys una plaça lliure.

1. Escollir un **Trajecte**.
2. Escollir una **Categoria** del trajecte.
3. Escollir un **Vagó** de la categoria.
4. Veure el **mapa del vagó** i escollir un **Seient** lliure (el seient passa a *reservat*).
5. **Pagar** el bitllet.
6. Si el pagament és correcte, el seient passa a *ocupat* i s'imprimix el bitllet; si no, torna a quedar *lliure*.

#### Exemple de bitllet

```
* ************ BITLLET TREN ************* *
* Origen TRAJECTE:  VLC
* Destí  TRAJECTE:  BCN
* Horari TRAJECTE:  01/06/2027 08:30
* Duració TRAJECTE: 120 min
* --------- DADES DEL SEIENT ----------- *
* TREN:      tren01
* CATEGORIA: Turista
* VAGÓ:      vago02
* Seient:    07A
* Preu:      160 €
* ************************************** *
```

---

### Menú de l'aplicació

```
Menú aplicació RENFE:
====================
 1. Crear VAGÓ
 2. Modificar VAGÓ
 3. Eliminar VAGÓ
 4. Crear CATEGORIA
 5. Modificar CATEGORIA
 6. Eliminar CATEGORIA
 7. Crear TREN
 8. Modificar TREN
 9. Eliminar TREN
10. Crear TRAJECTE
11. Modificar TRAJECTE
12. Eliminar TRAJECTE
13. Associar CATEGORIES/VAGONS a TRAJECTE
14. Comprar BITLLET
 0. Eixir aplicació RENFE
```

---

### Llicència

Obra publicada sota llicència **Reconeixement – NoComercial – CompartirIgual (CC BY-NC-SA)**: no es permet un ús comercial de l'obra original ni de les obres derivades, i la seua distribució ha de fer-se amb una llicència igual a la de l'obra original.

[Pujar](#top)

## 1. Projecte Ferroviari versio 1.1 bitllet sense fils (v1.1) <a name="v11"></a>

Versió del projecte en què sols es demana implementar els mètodes sense cos, és a dir, aquells mètodes on sols està declarada la interfície (***pagamentBitllet(BigDecimal preu)*** i ***compraBitlletTrajecte()***)

### 1.1. Conceptes treballats

- Simulació d'una espera amb `Thread.sleep()` i tractament de l'excepció comprovada `InterruptedException`.
- Cicle de vida d'un seient: *lliure* → *reservat* → *ocupat* (si es paga) o de nou *lliure* (si no es paga).
- Validació de l'entrada per teclat amb la classe `Validacio` i cancel·lació amb `0`.
- Recorregut guiat per les relacions entre objectes: Trajecte → Categoria → Vagó → Seient.

### 1.2. Canvis de la v1.0 a la v1.1. 

- Només canvia la classe **CompanyiaFerroviaria**. Les altres sis classes són idèntiques, llevat del nom del paquet.

Mètodes: 
- ***pagamentBitllet(BigDecimal preu)***: 
  - a l’esquelet (v1.0) és un TODO que retorna *false*. 
  - A la v1.1:
    - mostra l’import;
    - espera 3 s amb *Thread.sleep*, i si l’espera s’interromp considera que el pagament no s’ha fet;
    - demana la confirmació amb **Validacio.validaBoolea**.
- ***compraBitlletTrajecte()***: 
  - a l’esquelet només hi ha els 10 passos en comentaris. 
  - A la v1.1 estan implementats:
    - tria el trajecte i comprova que tinga tren;
    - tria la categoria i el vagó, i comprova que el vagó tinga seients lliures;
    - mostra el mapa;
    - demana el seient fins que en tria un de lliure. Amb 0 es cancel·la, i un seient ocupat no es toca mai.
    - reserva el seient i demana el pagament: si es paga, el seient queda ocupat i s’imprimeix el bitllet; si no, el seient s’allibera;
    - torna a mostrar el mapa.
- ***Comentari de capçalera***: canvia de «v1.0 ESQUELET» a «v1.1».

### 1.3. On trobar la solució

- **Carpeta:** [`TRENv11_Compra1Bitllet`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v11_Compra1Bitllet) (paquet `RENFE_v11_Compra1Bitllet`).
- **Etiqueta:** `v1.1`. Pots descarregar el codi d'aquesta versió des de la [release v1.1](../../releases/tag/v1.1) o, si has clonat el repositori, amb `git checkout v1.1`.
- **Canvis detallats:** el fitxer [`CANVIS.md`](TRENv11_Compra1Bitllet/CANVIS.md) explica pas a pas la implementació i les proves realitzades.
- **Dins del codi:** cada canvi respecte a l'esquelet està marcat amb un comentari `[v1.1]`. La capçalera de `CompanyiaFerroviaria` en conté el resum.

---
<a name="v21"></a>
## 2. Projecte Ferroviari (v2.1): compra d'1 bitllet amb fils

### 2.1. Contingut

La carpeta [`TRENv21_Compra1Bitllet_Threads`](TRENv21_Compra1Bitllet_Threads) (paquet `RENFE_v21_Compra1Bitllet_Threads`) conté la **solució del Treball 2**. Parteix de la versió 1.1 (`TRENv11_Compra1Bitllet`) i fa que la compra d'un bitllet (opció 14 del menú) s'execute en fils independents del fil principal:

- `FilCompraBitlletTrajecte` (nou): fil que fa la compra d'un bitllet. L'opció 14 en llança 2 alhora i el fil principal espera que acaben (`join()`).
- **Teclat compartit:** els mètodes de `Validacio` són `synchronized` i cada pregunta porta com a prefix el nom del fil (`[fil ppal]`, `[filCompraBitlletTrajecte1]`...).
- **Seients compartits:** `Vago` reserva, ocupa i allibera els seients amb mètodes `synchronized`, de manera que un seient no es pot vendre dues vegades.
- `ProvaConcurrencia` (nou, per al professorat): llança N fils que competeixen pels mateixos seients i responen automàticament.

### 2.2. Canvis respecte a la v1.1  

Aquest projecte (`TRENv21_Compra1Bitllet_Threads`, paquet `RENFE_v21_Compra1Bitllet_Threads`) és la **solució del Treball 2** (v2.1): la compra d'un bitllet es fa en fils independents del fil principal. Parteix de la v1.1 `TRENv11_Compra1Bitllet` (paquet `RENFE_v11_Compra1Bitllet`) d'aquest mateix repositori.

### 2.3. Resum dels canvis respecte a la v1.1

| Fitxer | Canvi |
|---|---|
| Tots | paquet `RENFE_v21_Compra1Bitllet_Threads` (abans `RENFE_v11_Compra1Bitllet`). |
| `Categoria`, `Seient`, `Trajecte`, `Tren` | Sense canvis (només el nom del paquet). |
| `CompanyiaFerroviaria` | Nom del fil principal, opció 14 amb fils (`llancaFilsCompra()`), prefix del fil a `pagamentBitllet()`; `compraBitlletTrajecte()` passa al fil. |
| `Vago` | Mètodes `synchronized` per als seients i eixida sense barrejar. |
| `Validacio` | Teclat sincronitzat i prefix amb el nom del fil. |
| `FilCompraBitlletTrajecte` | **Nou.** Compra d'un bitllet en 4 fases. |
| `ProvaConcurrencia` | **Nou.** Eina per al professorat. |

#### 2.3.1. `CompanyiaFerroviaria`

- **Nom del fil principal.** Al `main()` es posa `Thread.currentThread().setName("fil ppal")`. El nom ix com a prefix a cada pregunta.
- **Atributs nous:**
  - `NUM_FILS_COMPRA = 2`: el requisit 4 de l'enunciat demana almenys 2 fils;
  - `numFil`: comptador per numerar els fils.
- **Opció 14.** Crida `llancaFilsCompra()`, un mètode nou que:
  - crea els fils `filCompraBitlletTrajecte1`, `filCompraBitlletTrajecte2`…;
  - els llança;
  - espera que acaben amb `join()`, tal com permet el requisit 3.
- **`pagamentBitllet(BigDecimal)` es manté com a mètode propi**, igual que a la v1.1, i el criden els fils:
  - els missatges porten el prefix del fil;
  - el `Thread.sleep(3000)` es fa **sense cap bloqueig**, així que els pagaments de diversos fils es fan en paral·lel: si dos fils paguen, tarden uns 3 s en total, no 6;
  - només la confirmació S/N ocupa el teclat, el temps de llegir la resposta.
- **`compraBitlletTrajecte()`** es trasllada a `FilCompraBitlletTrajecte`.

#### 2.3.2. `Validacio`: el teclat és un recurs compartit

- **Monitor del teclat.** Tots els mètodes públics `valida...` són `public static synchronized`, i el monitor és `Validacio.class`. Així, mai no llegeixen dos fils alhora.
- **Mètode nou `prefix()`.** Retorna `"[" + nom del fil + "] "`.
  - `llegeixLinia()` el posa davant de cada pregunta, després dels salts de línia inicials.
  - El mètode nou `error()` el posa davant dels missatges d'error de `Validacio`.
- **Preguntes agrupades.** Un fil pot fer diverses preguntes seguides dins de `synchronized (Validacio.class) { ... }` sense que s'hi fique cap altre fil. Funciona perquè el fil que ja té el monitor de `Validacio.class` pot tornar a entrar als mètodes `static synchronized` de `Validacio` (els monitors de Java són **reentrants**).

#### 2.3.3. `Vago`: els seients són un recurs compartit

El monitor és **cada objecte `Vago`**, i dins d'aquests mètodes no es llig mai el teclat.

- **Mètodes nous:**
  - `synchronized boolean reservaSeient(Seient s)`: **comprova i reserva en una sola operació atòmica**, i retorna `false` si el seient ja no està lliure. Si es feren en dos passos, dos fils podrien veure el seient lliure i reservar-lo tots dos.
  - `synchronized void ocupaSeient(Seient s)`.
  - `synchronized void alliberaSeient(Seient s)`.
- **Mètodes que passen a ser `synchronized`:** `mapaVago()`, `numSeientsLliures()` i `teReserves()`.
- **Eixida sense barrejar:**
  - `mapaVago()` construeix el text amb un `StringBuilder` (és una «foto» coherent del vagó) i l'imprimeix d'una sola vegada;
  - `imprimirBitllet()` construeix el bitllet sencer, amb el prefix del fil, i també l'imprimeix d'una vegada.

#### 2.3.4. `FilCompraBitlletTrajecte` (nou)

`Runnable` amb el codi de compra de la v1.1, organitzat en fases. Els missatges porten el prefix del fil (mètode `missatge()`) i la reserva del seient es fa amb `vago.reservaSeient(triat)` en lloc de `triat.verificaSeient()` + `seient.reservaSeient()`.

| Fase | Què fa | Bloqueig |
|---|---|---|
| 1 | Passos 1-5: tria del trajecte, la categoria i el vagó, i mapa del vagó | `synchronized (Validacio.class)`: el fil manté el teclat tota la fase (i pren el `Vago` un moment per comptar els seients lliures i fer el mapa). |
| 2 | Pas 6: tria del seient i `vago.reservaSeient(triat)` | Teclat i, dins, el `Vago`. |
| 3 | Pagament: `CompanyiaFerroviaria.pagamentBitllet(preu)` | Cap durant el retard; el teclat només per a la confirmació S/N. |
| 4 | Passos 7-10: ocupar o alliberar el seient, imprimir el bitllet i mostrar el mapa | `Vago` (ocupar, alliberar i mapa). |

**Ordre dels bloquejos:** sempre teclat → vagó, mai a l'inrevés. Per això no hi pot haver interbloqueig.
