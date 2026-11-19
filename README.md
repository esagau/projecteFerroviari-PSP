# Projecte FERROVIARI (RENFE) – Compra de bitllets

Repositori de GitHub del mòdul *Programació de serveis i processos* (PSP) de l'IES Lluís Simarro, específic per al projecte FERROVIARI.

<a name="top"></a>

## Índex de continguts
* [1. Versions publicades](#versions)
* [2. Organització del repositori](#organitzacio)
* [3. Descripció del projecte](#descripcio)
* [4. Llicència](#llicencia)

<a name="versions"></a>
## 1. Versions publicades

Cada versió del projecte està en el seu propi **paquet** de codi i té associada una **etiqueta** (*tag*) de Git. A partir de la v1.1, cada paquet inclou un fitxer `CANVIS.md` que explica què canvia respecte a la versió anterior.

| Data | Etiqueta | Descripció | Paquet | Canvis |
| ---- | ---- | ---- | ---- | ---- |
| 31/10/2026 | [v1.0](../../releases/tag/v1.0) | Esquelet inicial del projecte (punt de partida del Treball 1) | [`TREN_v10_Compra1Bitllet`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v10_Compra1Bitllet) | — |
| 19/11/2026 | [v1.1](../../releases/tag/v1.1) | Solució del Treball 1: compra d'1 bitllet sense fils | [`TREN_v11_Compra1Bitllet`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v11_Compra1Bitllet) | [`CANVIS.md`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v11_Compra1Bitllet/CANVIS.md) |
| 19/11/2026 | [v2.1](../../releases/tag/v2.1) | Solució del Treball 2: compra d'1 bitllet amb fils | [`TREN_v21_Compra1Bitllet_Threads`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v21_Compra1Bitllet_Threads) | [`CANVIS.md`](iessimarro-dam2semi-psp2627_projecteFerroviari/src/TREN_v21_Compra1Bitllet_Threads/CANVIS.md) |

### 1.1. Com obtindre una versió concreta

- **Des de GitHub:** obri la *release* de la versió (enllaç de la columna «Etiqueta») i descarrega el codi en `.zip`.
- **Amb Git:** si ja has clonat el repositori, actualitza'l i situa't en l'etiqueta que vulgues:

```bash
git pull
git checkout v1.1
```

Per a tornar a la versió més recent, executa `git checkout main`.

[Pujar](#top)

<a name="organitzacio"></a>
## 2. Organització del repositori

```
README.md                        ← aquest fitxer: informació general del projecte
iessimarro-dam2semi-psp2627_projecteFerroviari/   ← projecte d'Eclipse (Spring Tools)
 └── src/
      ├── TREN_v10_Compra1Bitllet/   ← v1.0: esquelet
      ├── TREN_v11_Compra1Bitllet/   ← v1.1: solució del Treball 1
      │    └── CANVIS.md             ← canvis respecte a la v1.0
      └── TREN_v21_Compra1Bitllet_Threads/   ← v2.1: solució del Treball 2
           └── CANVIS.md             ← canvis respecte a la v1.1
```

- El projecte s'importa a Eclipse amb **File → Import → Existing Projects into Workspace**.
- Totes les versions comparteixen el mateix model de domini, les mateixes regles de negoci i el mateix menú, descrits en l'[apartat 3](#descripcio).
- Dins del codi, cada canvi respecte a la versió anterior està marcat amb un comentari amb el número de versió (per exemple, `[v1.1]`).

[Pujar](#top)

<a name="descripcio"></a>
## 3. Descripció del projecte

### 3.1. Introducció

Aplicació Java de consola per a la gestió ferroviària de trajectes de trens de llarga distància de la companyia **RENFE**, amb l'objectiu final de **comprar un bitllet** en una categoria concreta d'un tren que cobreix un trajecte entre dues ciutats, en un dia i hora determinats.

Material creat per **Enrique Niclós Camarasa**, per al mòdul *Programació de serveis i processos*.

> Aquest repositori conserva **diverses versions** del projecte, cadascuna en el seu propi paquet. Totes comparteixen el model de domini i les regles de negoci descrites ací.

---

### 3.2. Característiques principals

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

### 3.3. Model de domini

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

### 3.4. Regles de negoci

- Un **Trajecte** està cobert per **un únic Tren** i ofereix una o més **Categories**. Sense tren assignat no es poden afegir categories ni vendre bitllets.
- Una **Categoria** inclou un o més **Vagons** complets i fixa el **preu** dels seients.
- Cada **Vagó** pertany com a màxim a **una Categoria**, i cada **Categoria** a un únic **Trajecte**. Els no assignats es consideren *lliures*.
- Els seients pertanyen al Vagó; el seu nombre ha de ser **múltiple de 4** (màxim 100 per vagó).
- La suma de places de tots els vagons d'un trajecte **no pot superar la capacitat del Tren**. Es comprova en afegir categories o vagons, en modificar les places d'un vagó, en canviar el tren d'un trajecte i en reduir la capacitat d'un tren.
- **No es pot esborrar** un objecte en ús (vagó assignat, categoria assignada, tren que cobreix un trajecte o trajecte amb seients reservats o ocupats).
- **No es pot llevar ni modificar** un vagó, categoria o trajecte amb seients reservats o ocupats.
- Sobre un seient ocupat no es permet cap acció.
- Els identificadors han de ser únics dins de cada tipus d'objecte.

#### 3.4.1. Distribució dels seients

```
|  [1A] [1B]      [1C] [1D]  |
|  [2A] [2B]      [2C] [2D]  |
|   …    …         …    …    |
|  [nA] [nB]      [nC] [nD]  |
```

`A` i `D` són seients de finestra; `B` i `C`, de corredor.

---

### 3.5. Procés de compra

Per a poder comprar cal que existisca el trajecte, que tinga tren assignat, almenys una categoria, que la categoria tinga almenys un vagó i que el vagó tinga almenys una plaça lliure.

1. Escollir un **Trajecte**.
2. Escollir una **Categoria** del trajecte.
3. Escollir un **Vagó** de la categoria.
4. Veure el **mapa del vagó** i escollir un **Seient** lliure (el seient passa a *reservat*).
5. **Pagar** el bitllet.
6. Si el pagament és correcte, el seient passa a *ocupat* i s'imprimix el bitllet; si no, torna a quedar *lliure*.

#### 3.5.1. Exemple de bitllet

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

### 3.6. Menú de l'aplicació

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

[Pujar](#top)

<a name="llicencia"></a>
## 4. Llicència

Obra publicada sota llicència **Reconeixement – NoComercial – CompartirIgual (CC BY-NC-SA)**: no es permet un ús comercial de l'obra original ni de les obres derivades, i la seua distribució ha de fer-se amb una llicència igual a la de l'obra original.

[Pujar](#top)
