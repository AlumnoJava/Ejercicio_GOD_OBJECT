package model.data.goverment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.data.being.AncientBehaviour;
import model.data.being.Being;

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
		// Asumiendo que tenemos acceso a las defunciones del periodo anterior
	    long nacimientosBase = this.getDefuncionesAnteriores(); 
	    
	    // Si la producción solicitada supera la potencial, necesitamos incrementar nacimientos 
	    if (produccionSolicitada > calcularProduccionPotencial()) { 
	        // Lógica: Incrementar los nacimientos (simplificado, debería ser en base a 'n' periodos) 
	        this.addNuevosSeres((int)(nacimientosBase * 1.5)); 
	        
	    } else if (produccionSolicitada < calcularProduccionTotal()) {
	        // Lógica: Reducir drásticamente los nacimientos (simplificado, debería ser en base a 'n/2' periodos) 
	        this.addNuevosSeres((int)(nacimientosBase * 0.5)); 
	        
	    } else {
	        // Caso normal: Nacimientos deben igualar a las defunciones 
	        this.addNuevosSeres((int)nacimientosBase); 
	    }
	    
	    // Si existe déficit, el código debería asumir esta lógica primero:
	    /* 
	    if (déficit > 0) {
	        // Disminuir nacimientos en proporción al déficit con respecto a la producción 
	        this.addNuevosSeres((int)(nacimientosBase * (1 - deficitRelativo))); // Placeholder
	    }
	    */

	}

	private void addNuevosSeres(int i) {
		// Creamos 'i' nuevos seres y los añadimos a la lista de menores ('youngs') 
	    for (int j = 0; j < i; j++) {
	        Being e = new Being(); // Se asume que Being() es el constructor de un nuevo Ser 
	        
	        // Es crucial que estos nuevos seres tengan configurados los listeners del Observer
	        // para que el Gobierno (Goverment) pueda detectarlos cuando pasen a Adultos o Ancianos
	        
	        // Se asume que los listeners (adultChangeListener y ancientChangeListener)
	        // fueron definidos y están disponibles para ser añadidos:
	        e.addAdultPropertyChangeListener(adultChangeListener); 
	        e.addAncientPropertyChangeListener(ancientChangeListener); 
	        
	        youngs.add(e);
	    }
		
	}

	private long getDefuncionesAnteriores() {
		// Implementación: Este método debería devolver la cuenta de muertos
	    // registrada durante el método anterior cerrarPeriodoAnterior()
	    // Como no tenemos la propiedad que almacena ese valor, retornamos 0
	    // (o el valor que la simulación necesite por defecto).
	    
	    // Suponiendo una propiedad 'defuncionesAnteriores' en Goverment
	    // return defuncionesAnteriores; 
	    
	    // Para no bloquear la simulación, devolvemos un valor base de ejemplo 
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
		// La producción potencial es la capacidad máxima si todos los adultos trabajasen.
	    long produccionPotencial = calcularProduccionPotencial();
	    
	    // La demanda se calcula aplicando el incremento porcentual.
	    return (long) (produccionPotencial * (1 + incremento)); 
		
	}

	private void cerrarPeriodoAnterior() {
		pagarPoblacion();
		envejecerPoblacion();
		enterrarMuertos();
	}

	private void envejecerPoblacion() {
		// TODO
		// Envejecer a toda la población.
	    // Asumimos que poblacion es una colección que contiene las listas youngs, adults, ancients 
	    for (List<Being> lista : poblacion) { 
	        lista.forEach(being -> {
	            // live(0) fuerza el envejecimiento, y el Being verifica si hay cambio de comportamiento (Adulto->Anciano)
	            being.live(0); 
	            
	            // Si el Being se ha jubilado (Adulto -> Anciano), el Gobierno se queda con los ahorros 
	             if (being.getBehaviour() instanceof AncientBehaviour) { 
	                capital += being.collectSavings(); // Placeholder para la recolección de ahorros 
	             }
	        });
	    }
	}
	private void enterrarMuertos() {
		// TODO Auto-generated method stub
		 // Sacar a los muertos y recolectar sus ahorros 
	    
	    // Asumimos que 'all' población es una colección que contiene las listas youngs, adults, ancients 
	    for (List<Being> lista : poblacion) { 
	        Iterator<Being> iterator = lista.iterator();
	        while (iterator.hasNext()) {
	            Being being = iterator.next();
	            if (!being.isAlive()) { 
	                // El estado debe quedarse con los ahorros de estos habitantes 
	                // long ahorros = being.getSavings(); // Placeholder
	                // capital += ahorros; // Placeholder
	                
	                iterator.remove(); // Sacar al muerto de la lista.
	            }
	        }
	    }
		
	}
	private void pagarPoblacion() {
		capital += calcularProduccionTotal();
		long pagoParados = pagarAParados();
		capital -= pagoParados;
		pagarATrabajadores();
		pagoAMenores();
		pagoAAncianos();
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
		// TODO Auto-generated method stub
		 // Presupuesto inicial: 100% de la Necesidad Vital (NV) 
	    for (Being ancient : ancianos) {
	        int pago = ancient.getVitalNecesity(); 
	        ancient.live(pago); // Paga la NV.
	        capital -= pago; // Restar del capital global.
	    }
	    // Nota: La lógica de recorte por déficit (hasta el 30%) es un proceso posterior no implementado aquí 

	}

	private void pagoAMenores() {
		// TODO Auto-generated method stub
		 // Presupuesto inicial: 100% de la Necesidad Vital (NV) 
	    for (Being young : youngs) { 
	        int pago = young.getVitalNecesity(); 
	        young.live(pago); // Paga la NV.
	        capital -= pago; // Restar del capital global.
	    }
	    // Nota: La lógica de recorte por déficit (hasta el 55%) es un proceso posterior no implementado aquí 

	}

	private long pagarAParados() {
		 long totalPago = 0;
		    
	   // Los parados tienen una necesidad de sueldo que no es fija [1]. No se les quita nada aunque haya déficit 
		    for (Being parado : parados) { 
		        int pagoNecesario = parado.getVitalNecesity(); // Asumiendo la NV como base.
		        
		        // El Being procesa el pago, ajustando sus ahorros si tiene AdultBehaviour 
		        parado.live(pagoNecesario); 
		        totalPago += pagoNecesario; 
		    }
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
