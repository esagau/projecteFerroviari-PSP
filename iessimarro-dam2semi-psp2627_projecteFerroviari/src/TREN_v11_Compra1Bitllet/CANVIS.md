# Canvis de la v1.1 respecte a la v1.0

| | Versió anterior | Aquesta versió |
|---|---|---|
| **Versió** | v1.0: esquelet | v1.1: compra d'1 bitllet sense fils (**solució del Treball 1**) |
| **Paquet** | [`TREN_v10_Compra1Bitllet`](../TREN_v10_Compra1Bitllet) | `TREN_v11_Compra1Bitllet` |
| **Etiqueta** | [`v1.0`](../../../../../releases/tag/v1.0) | [`v1.1`](../../../../../releases/tag/v1.1) |

[Tornar al README](../../../README.md)

## 1. Què es demanava

En aquesta versió només calia implementar els dos mètodes de l'esquelet que no tenen cos, és a dir, aquells dels quals només hi ha declarada la capçalera:

- `pagamentBitllet(BigDecimal preu)`
- `compraBitlletTrajecte()`

## 2. Conceptes destacats

- Simulació d'una espera amb `Thread.sleep()` i tractament de l'excepció comprovada `InterruptedException`.
- Cicle de vida d'un seient: *lliure* → *reservat* → *ocupat* (si es paga) o de nou *lliure* (si no es paga).
- Validació de l'entrada per teclat amb la classe `Validacio` i cancel·lació amb `0`.
- Recorregut guiat per les relacions entre objectes: Trajecte → Categoria → Vagó → Seient.

## 3. Resum dels canvis

Només canvia la classe `CompanyiaFerroviaria`. Les altres sis classes (`Seient`, `Vago`, `Categoria`, `Trajecte`, `Tren` i `Validacio`) són idèntiques a les de la v1.0, llevat del nom del paquet i d'un comentari `[v1.1]` que ho indica.

| Fitxer | v1.0 (esquelet) | v1.1 (solució) |
|---|---|---|
| Tots | paquet `TREN_v10_Compra1Bitllet` | paquet `TREN_v11_Compra1Bitllet` |
| `CompanyiaFerroviaria` (capçalera) | «v1.0: ESQUELET per a l'alumnat» i l'avís del Treball 1 | «v1.1: compra d'1 bitllet» i la llista de canvis |
| `pagamentBitllet(BigDecimal preu)` | passos en comentaris `//TODO TREBALL 1`; retorna sempre `false` | implementat ([apartat 4](#4-pagamentbitlletbigdecimal-preu)) |
| `compraBitlletTrajecte()` | passos en comentaris `//TODO TREBALL 1`; mostra l'avís «PENDENT» | implementat ([apartat 5](#5-comprabitllettrajecte)) |

Dins del codi, cada canvi està marcat amb un comentari `[v1.1]`. La capçalera de `CompanyiaFerroviaria` en conté el resum.

## 4. `pagamentBitllet(BigDecimal preu)`

1. Mostra l'import a pagar i la durada del procés (`TEMPS_PAGAMENT / 1000` segons).
2. Simula la comunicació amb el banc amb `Thread.sleep(TEMPS_PAGAMENT)` (3 s). Encara no hi ha fils propis: qui s'espera és el fil principal (`main`).
3. `sleep()` pot llançar `InterruptedException`  i, per tant, cal capturar-la. Si l'espera s'interromp, es considera que el pagament **no** s'ha fet i es retorna `false`.
4. Demana la confirmació amb `Validacio.validaBoolea("Pagament confirmat? [S/N]")` i retorna el resultat.

## 5. `compraBitlletTrajecte()`

Segueix els 10 passos que donava l'esquelet en comentaris: Trajecte → Categoria → Vagó → Seient → Pagament. En qualsevol tria, introduir **0** cancel·la la compra.

1. **Trajectes.** Es mostren amb `llistaTrajectes()`. Si no n'hi ha cap, la compra acaba.
2. **Trajecte i tren.** L'usuari tria el trajecte i es mostra el seu detall i el del seu tren. El tren no es tria: és el del trajecte. Si el trajecte no té tren, s'informa i la compra acaba.
3. **Categoria.** Es mostren les categories del trajecte (`llistarCategoriesTrajecte()`) i l'usuari en tria una. Si el trajecte no en té, s'informa i la compra acaba.
4. **Vagó.** Es mostren els vagons de la categoria (`llistarVagonsCategoria()`) i l'usuari en tria un. Si no n'hi ha cap o si el vagó no té seients lliures (`numSeientsLliures() == 0`), s'informa i la compra acaba.
5. **Mapa.** Es mostra el mapa del vagó (`mapaVago()`).
6. **Seient.** Es demana el seient en el format fila + lletra («7A» o «07a») i s'obté amb `getSeient()`:
   - si no existeix al vagó, s'informa i es torna a demanar;
   - si no està lliure (`verificaSeient()` retorna `false`), s'informa del seu estat i es torna a demanar. **L'estat d'un seient no es pot tocar**, perquè és d'un altre client;
   - si està lliure, es reserva (`reservaSeient()`).
7. **Pagament.** Es crida `pagamentBitllet()` amb el preu de la categoria.
8. **Pagament correcte.** El seient passa a ocupat (`ocupaSeient()`) i s'imprimeix el bitllet (`imprimirBitllet(seient, categoria, trajecte)`).
9. **Pagament no fet.** La reserva s'allibera (`alliberaSeient()`) i el seient torna a estar lliure.
10. **Mapa final.** Es mostra el mapa resultant del vagó.


## 6. Proves realitzades

Cal validar la correcció del codi, per tant, s'han de fer proves basant-se en les dades inicials (`carregaDadesInicials()`):

| Prova | Resultat esperat i obtingut |
|---|---|
| **Compra completa:** VLC-MAD, Turista, `vago02`, seient 7A, pagat | S'imprimeix el bitllet, amb l'horari `01/06/2027 08:30`, i el mapa mostra `X` al 07A. |
| **Seient ocupat:** tornar a triar el 07a | Es rebutja («no està lliure (OCUPAT)») i el seient **continua ocupat**. |
| **Seient inexistent:** 99Z | S'informa que no existeix i es torna a demanar el seient. |
| **Pagament rebutjat:** seient 07B i resposta N | El seient torna a quedar lliure. |
| **Cancel·lació:** 0 en cada pas (trajecte, categoria, vagó i seient) | La compra acaba sense errors. |
| **Trajecte sense tren** | S'informa que el trajecte no té tren i la compra acaba. |

[Tornar al README](../../../README.md)
