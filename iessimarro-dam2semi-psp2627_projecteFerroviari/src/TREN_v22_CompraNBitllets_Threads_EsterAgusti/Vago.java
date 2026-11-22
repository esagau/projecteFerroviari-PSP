package TREN_v22_CompraNBitllets_Threads_EsterAgusti;
import java.util.ArrayList;

/**
 * Vagó d'un tren.
 *
 * Els seients es guarden en una matriu [fila][columna]. Totes les files tenen 4 seients:
 *     A (finestra)  B (corredor)  ||  C (corredor)  D (finestra)
 * Per això el nombre de places ha de ser múltiple de 4: nFiles = nPlaces / 4.
 *
 * Simplificació del model: els seients (i les reserves) són del Vagó.
 * Per això un Vagó només es pot assignar a UNA Categoria (atribut assignatVago).
 *
 * [v2.1] NOVETAT (CONCURRÈNCIA): diversos fils compren alhora. Els canvis d'estat dels seients
 * durant la compra es fan des dels mètodes "synchronized" del VAGÓ (el monitor és el propi objecte).
 */
public class Vago {

	//Nombre màxim de places que pot tindre un vagó (25 files)
	public static final int MAX_PLACES = 100;

	private String idVago;
	private int nPlaces;
	private boolean assignatVago;
	private Seient[][] seients;
	//Seients per filera: constant per a tots els vagons
	private final int nColumnes = 4;

	//Lletres dels seients d'una filera. L'ordinal (A=0, B=1...) és la columna de la matriu.
	public enum LletraSeient {A, B, C, D}

	//*****************************************************************
	//Constructor 1: vagó amb tots els seients LLIURES (s'usa a les dades inicials)
	public Vago(String idVago, int nPlaces) {
		this.idVago = idVago;
		this.nPlaces = nPlaces;
		this.assignatVago = false;
		creaSeients();
	}

	//*****************************************************************
	//Constructor INTERACTIU. L'identificador ja arriba validat des del menú
	//(CompanyiaFerroviaria.demanaIdentificador), la resta de dades es demanen ací.
	public Vago(String idVago) {
		System.out.println("Creació del VAGÓ " + idVago + "\n>>>>>>>>>>>>>>>>>>>");
		this.idVago = idVago;
		this.nPlaces = demanaPlaces(-1);
		this.assignatVago = false;
		creaSeients();
		System.out.println("<<<<<<<<<<<<<<<<<<<");
	}

	//*****************************************************************
	//Crea la matriu de seients, tots LLIURES, amb identificadors 01A, 01B... nnD
	private void creaSeients() {
		int nFiles = nPlaces / nColumnes;
		this.seients = new Seient[nFiles][nColumnes];
		for (int fila = 0; fila < nFiles; fila++)
			for (LletraSeient lletra : LletraSeient.values())
				this.seients[fila][lletra.ordinal()] = new Seient(String.format("%02d", fila + 1) + lletra);
	}

	//*****************************************************************
	//Demana el nombre de places fins que siga un múltiple de 4 entre 4 i MAX_PLACES.
	//Si defecte > 0, prement INTRO es manté eixe valor.
	private int demanaPlaces(int defecte) {
		int places;
		boolean valid;
		do {
			places = (defecte > 0)
					? Validacio.validaSencerDefecte("\tNombre de places del VAGÓ (múltiple de 4)?", MAX_PLACES, defecte)
					: Validacio.validaSencer("\tNombre de places del VAGÓ (múltiple de 4)? ", MAX_PLACES);
			valid = places > 0 && places % nColumnes == 0;
			if (!valid)
				System.out.println("\tERROR: Les places han de ser un múltiple de 4 entre 4 i " + MAX_PLACES);
		} while (!valid);
		return places;
	}

	//*****************************************************************
	//Modifica el nombre de places. Només es pot fer si el vagó no té cap seient
	//RESERVAT ni OCUPAT, perquè la matriu de seients es torna a crear (es perdrien les vendes).
	public void modificaVago() {
		System.out.println("\tModificació del VAGÓ " + idVago + "\n\tPrem INTRO per mantindre el valor anterior");

		if (teReserves()) {
			System.out.println("\tERROR: El VAGÓ té seients reservats o ocupats: no es pot modificar");
			return;
		}

		//Si el vagó està en un trajecte, el nou nombre de places no pot fer
		//que el trajecte supere la capacitat del seu tren.
		int maxPlaces = MAX_PLACES;
		Trajecte trajecte = CompanyiaFerroviaria.trajecteDeVago(this);
		if (trajecte != null && trajecte.getTren() != null) {
			int placesAltresVagons = trajecte.placesTrajecte() - this.nPlaces;
			maxPlaces = Math.min(MAX_PLACES, trajecte.getTren().getCapacitat() - placesAltresVagons);
		}

		int novesPlaces;
		do {
			novesPlaces = demanaPlaces(this.nPlaces);
			if (novesPlaces > maxPlaces)
				System.out.println("\tERROR: Amb " + novesPlaces + " places el trajecte " + trajecte.getIdTrajecte()
						+ " supera la capacitat del tren (màxim per a aquest vagó: " + maxPlaces + ")");
		} while (novesPlaces > maxPlaces);

		if (novesPlaces != this.nPlaces) {
			this.nPlaces = novesPlaces;
			creaSeients();
		}
		System.out.println(this);
	}

	//*****************************************************************
	public void esborraVago() {
		System.out.println("VAGÓ " + idVago + " esborrat!");
	}

	//*****************************************************************
	//Mostra el mapa de seients del vagó amb la icona de l'estat de cada seient.
	//[v2.1] Ara és synchronized: es construeix el text amb StringBuilder i s'imprimeix
	//d'una sola vegada, perquè no es barrege amb l'eixida d'altres fils.
	public synchronized void mapaVago() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t=============  MAPA VAGO ").append(idVago).append("  ================\n");
		sb.append("\t Seient-> ");
		for (LletraSeient lletra : LletraSeient.values())
			sb.append(" ").append(lletra).append(" ");
		sb.append("\n");

		for (int fila = 0; fila < seients.length; fila++) {
			sb.append("\t Fila ").append(String.format("%02d", fila + 1)).append(": ");
			for (int col = 0; col < nColumnes; col++)
				sb.append(" ").append(seients[fila][col].iconaSeient()).append(" ");
			sb.append("\n");
		}
		sb.append("\n\t SIMBOLOGIA: X=ocupat; O=lliure; ?=reservat\n");
		sb.append("\t==============================================");
		System.out.println(sb);
	}

	//*****************************************************************
	//Mostra el bitllet de tren amb totes les dades de la compra.
	//El tren i el vagó s'obtenen del trajecte i de l'objecte actual (this).
	//[v2.1] El bitllet es construeix sencer (amb el nom del fil) i s'imprimeix d'una vegada.
	public void imprimirBitllet(Seient s, Categoria c, Trajecte tra) {
		String bitllet = Validacio.prefix() + "Imprimint el seu Bitllet de tren...\n"
				+ "******************************************\n"
				+ "* ********** BITLLET TREN ************** *\n"
				+ "* Origen TRAJECTE:  " + tra.getOrigen() + "\n"
				+ "* Destí  TRAJECTE:  " + tra.getDesti() + "\n"
				+ "* Horari TRAJECTE:  " + tra.mostraDataFormatada() + "\n"
				+ "* Duració TRAJECTE: " + tra.getDuracio() + " min\n"
				+ "* --------- DADES DEL SEIENT ------------ *\n"
				+ "* TREN:      " + tra.getTren().getIdTren() + "\n"
				+ "* CATEGORIA: " + c.getNomCategoria() + "\n"
				+ "* VAGÓ:      " + this.idVago + "\n"
				+ "* Seient:    " + s.getIdSeient() + "\n"
				+ "* Preu:      " + c.getPreu() + " €\n"
				+ "******************************************";
		System.out.println(bitllet);
	}

	//*****************************************************************
	//[v2.1] MÈTODES NOUS: OPERACIONS CONCURRENTS SOBRE ELS SEIENTS
	//El monitor és el propi vagó: dos fils no poden canviar alhora els seients
	//d'un mateix vagó. Dins d'aquests mètodes MAI es llig el teclat.
	//*****************************************************************

	//RESERVA el seient només si està LLIURE i retorna si l'ha pogut reservar.
	//Comprovar + reservar és una operació ATÒMICA: si es feren en dues crides separades,
	//dos fils podrien veure el seient lliure i reservar-lo tots dos.
	public synchronized boolean reservaSeient(Seient s) {
		if (!s.verificaSeient())
			return false;
		s.reservaSeient();
		return true;
	}

	//OCUPA el seient reservat (el pagament s'ha fet)
	public synchronized void ocupaSeient(Seient s) {
		s.ocupaSeient();
	}

	//ALLIBERA el seient reservat (el pagament no s'ha fet)
	public synchronized void alliberaSeient(Seient s) {
		s.alliberaSeient();
	}
	
	
	//Retorna si algun seient de la llista ja està OCUPAT (venut per un altre fil)
	public synchronized boolean algunOcupat(ArrayList<Seient> seients) {
		for (Seient s : seients)
			if (!s.verificaSeient())
				return true;
		return false;
	}
	
	
	//Comprova que cap seient s'ha ocupat i els ocupa tots.
	//Retorna false (sense ocupar res) si algun ja estava ocupat: tot o res.
	public synchronized boolean confirmaSeients(ArrayList<Seient> seients) {
		if (algunOcupat(seients))
			return false;
		for (Seient s : seients)
			s.ocupaSeient();
		return true;
	}
	
	//Lleva NOMÉS les reserves d'esta compra
	public synchronized void alliberaSeients(ArrayList<Seient> seients) {
		for (Seient s : seients)
			s.alliberaSeient();
		seients.clear();
	}
		
		


	//*****************************************************************
	//Retorna el seient a partir del seu identificador ("7A", "07a"...),
	//o null si el format no és correcte o el seient no existeix al vagó.
	public Seient getSeient(String idSeient) {
		String id = idSeient.trim().toUpperCase();
		//1 o 2 xifres per a la fila + una lletra entre A i D
		if (!id.matches("^\\d{1,2}[A-D]$"))
			return null;
		int fila = Integer.parseInt(id.substring(0, id.length() - 1));
		int columna = LletraSeient.valueOf(id.substring(id.length() - 1)).ordinal();
		if (fila < 1 || fila > seients.length)
			return null;
		return seients[fila - 1][columna];
	}

	//*****************************************************************
	//Retorna quants seients LLIURES+reservats té el vagó
	//[v2.1] Ara és synchronized: compta els seients mentre cap altre fil els canvia.
	public synchronized int numSeientsLliures() {
		int lliures = 0;
		for (Seient[] fila : seients)
			for (Seient s : fila)
				if (s.verificaSeient())
					lliures++;
		return lliures;
	}

	//*****************************************************************
	//Retorna si algun seient està RESERVAT o OCUPAT (és a dir, si no tots estan lliures)
	//[v2.1] Ara és synchronized.
	public synchronized boolean teReserves() {
		for (Seient[] fila : seients)
			for (Seient s : fila)
				if (s.getDisponibilitat() != Seient.Estat.LLIURE)
					return true;
		return false;
	}

	//*****************************************************************
	@Override
	public String toString() {
		return "VAGÓ " + idVago + ": " + nPlaces + " places (" + seients.length + " files), "
				+ numSeientsLliures() + " lliures, " + (assignatVago ? "assignat" : "LLIURE");
	}

	//GETTERS & SETTERS
	//*****************************************************************
	public String getIdVago() {
		return idVago;
	}

	public void setIdVago(String idVago) {
		this.idVago = idVago;
	}

	public int getnPlaces() {
		return nPlaces;
	}

	public int getnColumnes() {
		return nColumnes;
	}

	public boolean isAssignatVago() {
		return assignatVago;
	}

	public void setAssignatVago(boolean assignatVago) {
		this.assignatVago = assignatVago;
	}

	public Seient[][] getSeients() {
		return seients;
	}
}
