package model.data.being;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;

import utiles.Utiles;

public class Being {
	private static final int maxLife = 120;
	private static final int minLife = 0;
	public static final int ADULTAGE = 18;
	public static final int ANCIENTAGE = 65;

	private float lifeExpectancy;
	protected int currentAge = 0;
	protected static int vitalNecesity = 100;
	Behaviour behaviour;
	// patron observer
	PropertyChangeSupport propertyAdultChangeSupport;
	PropertyChangeSupport propertyAncientChangeSupport;
	// patron observer

	public Being(float lifeExpectancy) {
		super();
		this.lifeExpectancy = lifeExpectancy;
		setBehaviour(new YoungBehaviour(this));
		// patron observer
		propertyAdultChangeSupport = new PropertyChangeSupport(this);
		propertyAncientChangeSupport = new PropertyChangeSupport(this);
		// patron observer
	}

	public Being() {
		this(calculateLifeExpectancy(minLife, maxLife));
	}

	// Observer
	public void addAdultPropertyChangeListener(PropertyChangeListener listener) {
		propertyAdultChangeSupport.addPropertyChangeListener(listener);
	}

	public void removeAdultPropertyChangeListener(PropertyChangeListener listener) {
		propertyAdultChangeSupport.removePropertyChangeListener(listener);
	}

	public void addAncientPropertyChangeListener(PropertyChangeListener listener) {
		propertyAncientChangeSupport.addPropertyChangeListener(listener);
	}

	public void removeAncientPropertyChangeListener(PropertyChangeListener listener) {
		propertyAncientChangeSupport.removePropertyChangeListener(listener);
	}

	// Observer
	public void promoteToAdult() {
		this.propertyAdultChangeSupport.firePropertyChange("adult", null, this);
	}

	public void promoteToAncient() {
		this.propertyAncientChangeSupport.firePropertyChange("ancient", null, this);
	}

	public Behaviour getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	public float getLifeExpectancy() {
		return lifeExpectancy;
	}

	public void setLifeExpectancy(float lifeExpectancy) {
		this.lifeExpectancy = lifeExpectancy;
	}

	private static int calculateLifeExpectancy(int minimun, int maximum) {
		return Utiles.dameNumero(maximum);
	}

	public int getCurrentAge() {
		return currentAge;
	}

	public void setCurrentAge(int currentAge) {
		this.currentAge = currentAge;
	}

	protected void aging() {
		this.currentAge++;
	}

	public boolean isAlive() {
		return this.currentAge <= this.lifeExpectancy;
	}

	public boolean live(int salary) {
		this.aging();
		behaviour.feed(salary);
		Optional<CheckableBehaviour> checkable = behaviour.getCheckable();
		if (checkable.isPresent())
			checkable.get().checkChangeBehaviour();
		return isAlive();
	}

	protected void recalculateLifeExpectancy(int salary) {
		float percentage = salary * 100 / this.vitalNecesity;
		float maxPercentage = 99;
		float minPercentage = 33;
		float minPeriod = 1f;
		if (percentage <= maxPercentage && percentage >= minPercentage) {
			this.lifeExpectancy -= minPeriod;
		} else if (percentage <= minPercentage) {
			float periodoMax = 2f;
			this.lifeExpectancy -= periodoMax;
		}
	}

	public boolean becomeOlder() {
		return isAlive() && currentAge == ANCIENTAGE;
	}

	public boolean becomeAdult() {
		return isAlive() && currentAge == ADULTAGE;
	}

	public int getVitalNecesity() {
		return vitalNecesity;
	}

	public void setActive(boolean b) {
		// TODO Auto-generated method stub
		 // 1. Obtener el comportamiento revisable (CheckableBehaviour)
	    Optional<CheckableBehaviour> checkable = behaviour.getCheckable();

	    // 2. Comprobar si existe y si es el comportamiento AdultBehaviour
	    if (checkable.isPresent() && behaviour instanceof AdultBehaviour) {
	        // 3. Delegar la llamada al AdultBehaviour para establecer el estado activo/parado
	        ((AdultBehaviour) behaviour).setActive(b);

	        // Nota: La clase Goverment llama a setActive(true) cuando contrata 
	        // y a setActive(false) cuando despide 
	    }
		
	}

	public long collectSavings() {
		// TODO Auto-generated method stub
		 // 1. Obtener el comportamiento revisable (CheckableBehaviour)
	    Optional<CheckableBehaviour> checkable = behaviour.getCheckable(); 

	    // 2. Comprobar si existe y si es el AdultBehaviour (el único que tiene ahorros)
	    if (checkable.isPresent() && behaviour instanceof AdultBehaviour) {
	        AdultBehaviour adultBehaviour = (AdultBehaviour) behaviour;

	        // 3. Recoger los ahorros
	        long ahorrosRecogidos = adultBehaviour.getSavings(); 

	        // 4. Poner los ahorros a cero en el comportamiento del adulto, ya que han sido recolectados
	        adultBehaviour.setSavings(0); 

	        // 5. Retornar la cantidad recolectada
	        return ahorrosRecogidos;
	    }
	    // Si no es un AdultBehaviour (ej. es YoungBehaviour o AncientBehaviour),
	    // no tiene ahorros para recolectar.
	    return 0;
	}
}
