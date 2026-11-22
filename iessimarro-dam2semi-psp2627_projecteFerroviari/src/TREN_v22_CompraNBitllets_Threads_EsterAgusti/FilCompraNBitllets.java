package TREN_v22_CompraNBitllets_Threads_EsterAgusti;
import java.util.ArrayList;

/**
 * Fil que fa la COMPRA D'UN BITLLET. L'opció 14 del menú en llança diversos alhora.
 *
 * [v2.1] CLASSE NOVA. Conté el mètode compraBitlletTrajecte() que a la v1.1 era a
 * CompanyiaFerroviaria, amb els mateixos passos 1-10, però organitzat en 4 FASES
 * perquè diversos fils puguen comprar alhora sense barrejar el teclat ni els seients:
 *   FASE 1 (passos 1-5)  trajecte, categoria, vagó i mapa  -> amb el monitor del teclat
 *   FASE 2 (pas 6)       tria i reserva del seient          -> teclat i, dins, el Vago
 *   FASE 3               pagament                           -> sense bloqueig durant el retard
 *   FASE 4 (passos 7-10) ocupar/alliberar, bitllet i mapa   -> monitor del Vago
 * Diferència clau amb la v1.1: el seient es reserva amb vago.reservaSeient(triat)
 * (comprovar + reservar en una sola operació atòmica) en lloc de
 * triat.verificaSeient() + seient.reservaSeient().
 *
 * Recursos compartits i monitors:
 *  - TECLAT  -> monitor de Validacio.class. Les preguntes d'una mateixa fase es fan dins de
 *               synchronized (Validacio.class) { ... } perquè cap altre fil s'hi intercale.
 *  - SEIENTS -> monitor de cada objecte Vago (mètodes synchronized del Vago).
 *
 * Ordre dels bloquejos: es pot demanar el monitor del Vago tenint el del teclat,
 * però MAI a l'inrevés (dins del Vago no es llig el teclat). Així no hi ha interbloqueig.
 * El retard del pagament es fa SENSE cap bloqueig: els fils paguen en paral·lel.
 */
public class FilCompraNBitllets implements Runnable {

	@Override
	public void run() {
		compraNBitlletsTrajecte();
	}

	//*****************************************************************
	//COMPRA INTERACTIVA D'UN BITLLET
	//Ordre lògic: Trajecte -> Categoria -> Vagó -> Seient -> Pagament.
	//En qualsevol pas, si no hi ha opcions o l'usuari tria 0, la compra acaba sense fer res.
	private void compraNBitlletsTrajecte() {
		Trajecte trajecte;
		Categoria categoria;
		Vago vago;
		int nBitllets;

		//==== FASE 1: TRAJECTE, CATEGORIA i VAGÓ (el fil manté el TECLAT tota la fase) ====
		synchronized (Validacio.class) {
			//1. Trajectes disponibles
			missatge("Inici de la compra de bitllets");
			if (CompanyiaFerroviaria.llistaTrajectes() == 0)
				return;

			//2. Tria del trajecte i detall del trajecte i del seu tren
			int opcio = Validacio.validaSencer("\tTria TRAJECTE [0 = cancel·lar]: ", CompanyiaFerroviaria.trajectes.size());
			if (opcio == 0)
				return;
			trajecte = CompanyiaFerroviaria.trajectes.get(opcio - 1);
			System.out.println("\n\t" + trajecte);
			if (trajecte.getTren() == null) {
				missatge("\tERROR: El TRAJECTE no té cap TREN assignat");
				return;
			}
			System.out.println("\t" + trajecte.getTren());

			//3. Categories del trajecte
			System.out.println("\nCATEGORIES del trajecte:");
			int numCategories = trajecte.llistarCategoriesTrajecte();
			if (numCategories == 0) {
				missatge("\tERROR: El TRAJECTE no té CATEGORIES");
				return;
			}
			opcio = Validacio.validaSencer("\tTria CATEGORIA [0 = cancel·lar]: ", numCategories);
			if (opcio == 0)
				return;
			categoria = trajecte.getCategoriesTrajecte().get(opcio - 1);

			//4. Vagons de la categoria
			System.out.println("\nVAGONS de la categoria:");
			int numVagons = categoria.llistarVagonsCategoria();
			if (numVagons == 0) {
				missatge("\tERROR: La CATEGORIA no té VAGONS");
				return;
			}
			opcio = Validacio.validaSencer("\tTria VAGÓ [0 = cancel·lar]: ", numVagons);
			if (opcio == 0)
				return;
			vago = categoria.getVagonsCategoria().get(opcio - 1);
			
			if (vago.numSeientsLliures() == 0) {
				missatge("\tERROR: El VAGÓ no té seients lliures");
				return;
			}

			//5. Mapa del vagó
			vago.mapaVago();
		}

		//==== FASE 2: tria i RESERVA del SEIENT ====
		//6. Es repeteix fins que se'n reserva un de LLIURE (0 = cancel·lar).
		//Comprovar i reservar es fa dins del Vago (atòmic): si un altre fil s'ha avançat,
		//reservaSeient() retorna false i es demana un altre seient.
		int disponibles = vago.numSeientsLliures();
		nBitllets = Validacio.validaSencer("\tQuants BITLLETS vols comprar (màxim " + disponibles + ") [0 = cancel·lar]: ", disponibles);
		if (nBitllets == 0) {
			missatge("\tCompra cancel·lada");
			return;
		}
			
			
		ArrayList<Seient> seients = new ArrayList<Seient>();
		synchronized (Validacio.class) {
			for (int i = 1; i <= nBitllets; i++) {
				missatge("\n\tBITLLET " + i + " de " + nBitllets);
				Seient seient = null;
				while (seient == null) {
					String idSeient = Validacio.validaCadena("\tSeient? (fila + lletra, p.e. 7A) [0 = cancel·lar]: ");
					if (idSeient.equals("0")) {
						vago.alliberaSeients(seients); //només les reserves d'esta compra
						missatge("\tCompra cancel·lada: seients alliberats");
						vago.mapaVago();
						return;
					}
					Seient triat = vago.getSeient(idSeient);
					if (triat == null)
						missatge("\tERROR: El seient " + idSeient + " no existeix en aquest vagó");
					else if (seients.contains(triat))
						missatge("\tERROR: Ja has triat el seient " + triat.getIdSeient() + " en esta compra");
					else if (!vago.reservaSeient(triat)) //atòmic: false = ocupat
						missatge("\tERROR: El seient " + triat.getIdSeient() + " ja està ocupat. Tria'n un altre");
					else {
						seient = triat;
						missatge("\tSeient " + seient.getIdSeient() + " RESERVAT");
					}
				}
				seients.add(seient);
			}
		}
		
		if (vago.algunOcupat(seients)) {
			missatge("\tERROR: Algun seient s'ha venut a un altre client. Compra anul·lada");
			vago.alliberaSeients(seients);
			vago.mapaVago();
			return;
		}

		//==== FASE 3: PAGAMENT (sense cap bloqueig mentre dura el retard) ====
		boolean pagat = CompanyiaFerroviaria.pagamentNBitllets(nBitllets, categoria.getPreu());

		//==== FASE 4: OCUPAR o ALLIBERAR el seient ====
		//7-9. Si es paga, el seient queda OCUPAT i s'imprimeix el bitllet;
		//si no, la reserva s'allibera i el seient torna a estar disponible.
		if (pagat) {
			if (vago.confirmaSeients(seients)) {
				for (Seient s : seients) {
					missatge("\tSeient " + s.getIdSeient() + " OCUPAT");
					vago.imprimirBitllet(s, categoria, trajecte);
				}
			} else {
				missatge("\tERROR: Un altre client ha confirmat abans algun dels seients."
						+ " La compra no s'ha pogut fer");
				vago.alliberaSeients(seients);
			}
		} else {
			vago.alliberaSeients(seients);
			missatge("\tPagament no realitzat: les teues reserves s'han alliberat");
		}

		//10. Mapa resultant
		vago.mapaVago();
	}

	//*****************************************************************
	//Missatge per pantalla amb el nom del fil com a prefix
	private static void missatge(String text) {
		System.out.println(Validacio.prefix() + text);
	}
}//class
