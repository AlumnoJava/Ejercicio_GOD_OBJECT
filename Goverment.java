package model.data.goverment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.data.being.AncientBehaviour;
import model.data.being.Being;
import model.data.being.GestionNacimientosStrategy;
import model.data.being.IncrementarNacimientosStrategy;
import model.data.being.NacimientosBaseStrategy;
import model.data.being.ReducirNacimientosStrategy;

public class Goverment {
	private int initialPopulation = 10;
	private PropertyChangeListener adultChangeListener, ancientChangeListener;
	// PAyDay
	
	private LinkedList<LinkedList<Being>> poblacion = new LinkedList<>();
	private LinkedList<Being> youngs;
	// esto es sustituido
	private final int produccionPorTrabajador = 400;
	private long produccionSolicitada;
	private long capital = 0;
	private LinkedList<Being> ancianos;
	// sutituyen a adultos
	private LinkedList<Being> adults;
	private LinkedList<Being> trabajadores;
	private LinkedList<Being> parados;
	
	public Goverment(int initialPopulation) {
		this.initialPopulation = initialPopulation;
		prepareState(initialPopulation);
	}

	public Goverment(int initialPopulation, int lifeExpenctacy) {
		this.initialPopulation = initialPopulation;
		prepareState(initialPopulation, lifeExpenctacy);
	}

	public Goverment() {
		super();
		prepareState(initialPopulation);

	}

	// funciones a realizar
	public void gestionarPeriodo(float incremento) {
		cerrarPeriodoAnterior();
		abrirPeriodoActual(incremento);
	}

	private void abrirPeriodoActual(float incremento) {
		produccionSolicitada = calculaDemanda(incremento);
		long diferencia = produccionSolicitada - calcularProduccionTotal();
		gestionEmpleados(diferencia);
		gestionNacimientos();
	}

	private void gestionNacimientos() {
		 long nacimientosBase = this.getDefuncionesAnteriores(); 
		 GestionNacimientosStrategy estrategiaSeleccionada = 
	   (produccionSolicitada > calcularProduccionPotencial()) 
		? new IncrementarNacimientosStrategy()
		: (produccionSolicitada < calcularProduccionTotal())
		  ? new ReducirNacimientosStrategy()
				            
// Caso normal [5]
		 : new NacimientosBaseStrategy();

 // Aplicar la estrategia
		 int nuevosSeres = estrategiaSeleccionada.calcularNacimientos(nacimientosBase);
		 this.addNuevosSeres(nuevosSeres); 

	}

	private void addNuevosSeres(int i) {
		// Creamos 'i' nuevos seres y los añadimos a la lista de menores ('youngs') 
	    for (int j = 0; j < i; j++) {
	        Being e = new Being(); 
	        e.addAdultPropertyChangeListener(adultChangeListener); 
	        e.addAncientPropertyChangeListener(ancientChangeListener); 
	        
	        youngs.add(e);
	    }
		
	}

	private long getDefuncionesAnteriores() {
	    return 5; 
	}

	private void gestionEmpleados(long diferencia) {
		 // Calculamos cuántos seres son necesarios u obsoletos, basándonos en la producción individual.
	    int personasNecesariasOSobrantes = (int) (Math.abs(diferencia) / produccionPorTrabajador);

	    if (diferencia > 0) { // Demanda > Producción: Necesitamos emplear
	        
	        int aEmplear = Math.min(personasNecesariasOSobrantes, parados.size());
	        
	        for (int i = 0; i < aEmplear; i++) {
	            // Mover de parados a trabajadores.
	            Being being = parados.remove(0);
	            trabajadores.add(being); 
	            being.setActive(true); // Se asume un método para activar el estado laboral 
	        }

	    } else if (diferencia < 0) { // Demanda < Producción: Necesitamos despedir
	        
	        int aDespedir = Math.min(personasNecesariasOSobrantes, trabajadores.size());

	        for (int i = 0; i < aDespedir; i++) {
	            // Mover de trabajadores a parados.
	            Being being = trabajadores.remove(trabajadores.size());
	            parados.add(being); 
	            being.setActive(false); // Se asume un método para desactivar el estado laboral 
	        }
	    }
	}

	

	private long calculaDemanda(float incremento) {
	    long produccionPotencial = calcularProduccionPotencial();
	    return (long) (produccionPotencial * (1 + incremento)); 
		
	}

	private void cerrarPeriodoAnterior() {
		pagarPoblacion();
		envejecerPoblacion();
		enterrarMuertos();
	}

	private void envejecerPoblacion() {
	    poblacion.forEach(lista -> {
	        lista.forEach(being -> {
	            being.live(0); 
	            
	            if (being.getBehaviour() instanceof AncientBehaviour) { // Comprobación de tipo 
	                capital += being.collectSavings(); // Recolección de ahorros 
	            }
	        });
	    });
	}
	private void enterrarMuertos() {
	    poblacion.forEach(lista -> {
	        lista.removeIf(being -> {
	            if (!being.isAlive()) {
	                capital += being.collectSavings(); 
	                return true; 
	            }
	            return false; 
	        });
	    });
		
	}
	private void pagarPoblacion() {
		 capital += calcularProduccionTotal(); // Aumentar capital con la producción total 
		    long pagoParados = pagarAParados(); // Pago a parados (su lógica es interna) 
		    capital -= pagoParados; 
		    // Pago a Trabajadores
		    long sueldoTrabajador = produccionPorTrabajador; // Asumiendo que esta es la cantidad fija 
		    trabajadores.forEach(trabajador -> {
		        trabajador.live((int)sueldoTrabajador);
		    });

		    capital -= (long)trabajadores.size() * sueldoTrabajador; 
		    pagoAAncianos(); // 
		    pagoAMenores(); 
	}

	private int calcularProduccionTotal() {
		return trabajadores.size() * produccionPorTrabajador;
	}
	
	private int calcularProduccionPotencial() {
		return adults.size()*produccionPorTrabajador;
	}

	private void pagarATrabajadores() {
		// Los trabajadores son pagados con un sueldo (asumido como produccionPorTrabajador) 
	    int sueldoTrabajador = produccionPorTrabajador; 
	    
	    for (Being trabajador : trabajadores) { 
	        // El método live gestiona si el trabajador ahorra o se endeuda con la NV 
	        trabajador.live(sueldoTrabajador); 
	        capital -= sueldoTrabajador; // Reducción del capital por el pago.
	    }
	    //  La reducción por déficit hasta la NV si el déficit persiste se aplicaría después 

	}

	private void pagoAAncianos() {
		 // Presupuesto inicial: 100% de la Necesidad Vital (NV) 
	    ancianos.forEach(ancient -> {
	        int pago = ancient.getVitalNecesity(); 
	        ancient.live(pago); // Paga la NV. [18]
	        capital -= pago; // Restar del capital global. 
	    });

	}

	private void pagoAMenores() {
	    youngs.forEach(young -> {
	        int pago = young.getVitalNecesity(); 
	        young.live(pago); // Paga la NV. [18]
	        capital -= pago; // Restar del capital global. 
	    });

	}

	private long pagarAParados() {
		 long totalPago = parados.stream() 
	        .mapToLong(Being::getVitalNecesity) 
	        .sum();
	    
	   
	    parados.forEach(parado -> {
	        int pagoNecesario = parado.getVitalNecesity();
	        parado.live(pagoNecesario);
	    });

	    return totalPago; 
	}

	// payday
	public LinkedList<Being> getYoungs() {
		return youngs;
	}

	public LinkedList<Being> getAncients() {
		return ancianos;
	}
	//PayDay
	

	

	private void createListeners() {
		adultChangeListener = (being) -> {
			applyLambda(being, youngs, adults);
		};
		ancientChangeListener = (being) -> {
			applyLambda(being, adults, ancianos);
		};
	}

	private void applyLambda(PropertyChangeEvent being, List<Being> source, List<Being> destination) {
		Being remove = source.remove(source.indexOf(being.getNewValue()));
		destination.add(remove);
	}

	private void prepareState(int initialPop) {
		createLists();
		createListeners();
		raisePopulation(initialPop);
	}

	private void prepareState(int initialPop, int lifeExpenctacy) {
		createLists();
		createListeners();
		raisePopulation(initialPop, lifeExpenctacy);
	}

	private void createLists() {
		youngs = new LinkedList<>();
		ancianos = new LinkedList<>();
		poblacion = new LinkedList();
		adults = new LinkedList<>();
		poblacion.add(youngs);
		poblacion.add(adults);
		poblacion.add(ancianos);
		parados=new LinkedList<>();
		trabajadores=new LinkedList<>();
	}

	private void raisePopulation(int initialPopulation, int lifeExpenctancy) {
		for (int i = 0; i < initialPopulation; i++) {
			Being e = new Being(lifeExpenctancy);
			e.addAdultPropertyChangeListener(adultChangeListener);
			e.addAncientPropertyChangeListener(ancientChangeListener);
			youngs.add(e);
		}
	}

	private void raisePopulation(int initialPopulation) {
		for (int i = 0; i < initialPopulation; i++) {
			Being e = new Being();
			e.addAdultPropertyChangeListener(adultChangeListener);
			e.addAncientPropertyChangeListener(ancientChangeListener);
			youngs.add(e);
		}
	}

	public void developWorld(int years) {
		for (int i = 0; i < years; i++) {
			feed();
			bury();
		}
	}

	private void bury() {
		// sacar a los muertos
		poblacion.forEach(list -> list.removeIf(being -> !being.isAlive()));
	}

	// Resulta que un problema d e los streams es que no podemos borrar en una
	// coleccion mientras se
	// recorre.
	private void iterateCollectionForLiving(LinkedList<Being> colection) {
		LinkedList<Being> others = (LinkedList<Being>) colection.clone();
		for (Iterator<Being> iterator = others.iterator(); iterator.hasNext();) {
			Being being = (Being) iterator.next();
			being.live(being.getVitalNecesity());
		}
	}

	private void feed() {
		iterateCollectionForLiving(youngs);
		iterateCollectionForLiving(adults);
		iterateCollectionForLiving(ancianos);
	}

	public LinkedList<Being> getAdults() {
		return adults;
	}

}
