# Projecte FERROVIARI (RENFE) – Compra de bitllets

Repositori de Github del mòdul PSP de l'IES LLUIS SIMARRO específic per al projecte FERROVIARI

<a name="top"></a>

## Índex de continguts
* [Esquelet (v1.0)](#v10)
* [Projecte Ferroviari versió 1 ticket sense fils (v1.1)](#v11)



### Contingut del Repositori del projecte FERROVIARI:
| Data | Etiqueta | Descripcio|
| ---- | ---- | ---- |
| 31/09/2026 | [v1.0](#v10) | Esquelet inicial del projecte **TREN_v10_Esquelet**|

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

## Projecte Ferroviari versio 1.1 ticket sense fils (v1.1) <a name="v11"></a>

Versió del projecte en què sols es demana implementar els mètodes sense cos, és a dir, aquells mètodes on sols està declarada la interfície (***pagamentBitllet(BigDecimal preu)*** i ***compraBitlletTrajecte()***)

### Canvis de la v1.0 a la v1.1. 

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

---

