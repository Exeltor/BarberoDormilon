package semaforos;
import java.util.Random;

public class Customer extends Thread {
    private String name; 			// El nombre/id del cliente
    private BarberShop shop; 		// Instancia de la barberia asociada a este cliente
    private Random r;				// Generador de numeros aleatorios
    private boolean wants_haircut;	// Indica si el cliente quiere un corte de pelo

    /**
     * @param shop Instancia de la barberia a usar
     * @param name Nombre unico para este cliente
     */
    public Customer(BarberShop shop, String name) {
        this.name = name;
        this.shop = shop;
        wants_haircut = true;
        r = new Random();
    }


    public void run() {
        wasteTime();
        shop.customerReady(this);
    }

    /**
     * Comprueba si el cliente sigue queriendo un corte de pelo
     *
     * @return true if the customer wants a haircut, fals otherwise
     */
    public boolean wantsHaircut() {
        return wants_haircut;
    }

    /**
     * Indica que el cliente quiere irse. Esto hara que wantsHaircut()
     * devuelva falso de ahora en adelante.
     */
    public void wantsToLeave() {
        wants_haircut = false;
    }

    /**
     * El cliente permanecera en espera para un tiempo aleatorio. Esto asegura
     * que los clientes lleguen a tiempos aleatorios.
     */
    public void wasteTime() {
        try {
            Thread.sleep(Math.abs(r.nextInt(100000)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return name;
    }
}
