package semaforos;
import java.util.concurrent.Semaphore;

public class BarberShop {
    // semaforos
    private Semaphore barber_chair; // Silla del barbero
    private Semaphore chairs; 		// Sillas de la sala de espera
    private Semaphore barber;		// El propio barbero

    private int waiting_customers;	// Numero de clientes esperando
    private final int number_of_chairs; // Numero de sillas en la sala de espera

    private static final int HAIRCUT_TIME = 1000; // Tiempo requerido para un corte de pelo

    /**
     * @param chair_number tamaño de la sala de espera
     */
    public BarberShop(int chair_number) {
    	//La silla del barbero esta libre, el primer cliente la puede ocupar pero
    	//ningun otro cliente puede recibir un corte de pelo si no obtiene tanto al barbero
    	//y la silla del barbero
        barber_chair = new Semaphore(1, true);
        chairs = new Semaphore(0, true);
        barber = new Semaphore(0, true);

        number_of_chairs = chair_number;
        waiting_customers = 0;
        barberReady();
    }

    
    /*
     * Indica que el barbero esta listo para dar un corte de pelo. Notifica
     * al cliente que ha esperado el mayor tiempo
     */
    private void barberReady() {
        System.out.println("El barbero esta listo.");
        barber.release(); // Indicador de que el barbero esta libre
        
        //Si hay clientes en la cola de la sillas
        //el primero podra continuar.
        chairs.release();
        System.out.println("Permisos sillas disponibles =" +chairs.availablePermits() +", silla barbero =" +barber_chair.availablePermits());
    }

    /**
     * Indica que el cliente c esta listo para un corte de pelo.
     * Si el barbero esta ocupado, hace que el cliente se siente de vuelta.
     * Si el barbero esta libre, se adquiere el pase a la silla del barbero e
     * intenta obtener un corte de pelo.
     *
     * @param c El cliente que pide el pase.
     */
    public void customerReady(Customer c) {
        System.out.println(c + " quiere un corte de pelo");
        
        //Si el barbero esta ocupado, el cliente se sienta.
        //Al sentarse se bloquea el semaforo de las sillas.
        if(barber_chair.availablePermits() <= 0)
            customerSitDown(c);

        //Si el cliente quiere un corte de pelo, lo puede obtener.
        if(c.wantsHaircut()) {
            try {
            	//Primero debe adquirir el pase de la silla del barbero.
                barber_chair.acquire();
                haircut(c);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hace que el cliente c se siente en la sala de espera y que se bloquee hasta
     * que el barbero este disponible.
     *
     * @param c El cliente que esta iniciando la accion
     */
    public void customerSitDown(Customer c) {
    	/*
    	 * Si la barberia esta llena (todas las sillas estan ocupadas) el cliente se
    	 * enfada y se va. El hilo no se bloquea y vuelve a customerReady()
    	 */
        if(waiting_customers < number_of_chairs) {
            try {
                waiting_customers++;
                System.out.println(c + " Se sienta en la sala de espera. Hay " + waiting_customers + " clientes esperando");

                //El Thread estara bloqueado hasta que barberReady() suelte el semaforo de la silla
                chairs.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(c + " Se enfada y se va de la barberia");
            c.wantsToLeave();
        }
    }

    /**
     * Hace el corte de pelo. Suelta la silla de la sala de espera previamente ocupada
     * por el cliente c, adquiere el pase del barbero y recibe un corte de pelo.
     * Al finalizar, suelta la silla del barbero e indica que el barbero esta listo.
     *
     * @param c Cliente que inicia la accion.
     */
    public void haircut(Customer c) {
    	//Dado que el cliente esta recibiendo un corte de pelo, ya no ocupa una silla en la sala de espera.
        if(waiting_customers > 0)
            waiting_customers--;

        try {
            barber.acquire(); // Adquiere pase del barbero
            System.out.println(c + " esta recibiendo un corte de pelo");
            
            Thread.sleep(HAIRCUT_TIME);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println(c + " ha terminado y se va.");
        barber_chair.release(); // Suelta la silla del barbero
        barberReady(); // Soltara al barbero y la silla de la sala de espera para que un cliente pueda continuar.
    }

    public static void main(String[] args) {
        // Sorry, no command line argument parser
        // please edit the line below to change the
        // number of visiting customers
        int customer_number = 100;
        BarberShop barberia = new BarberShop(5);
        Thread[] cliente = new Thread[customer_number];

        for(int i=0; i<customer_number; i++)
            cliente[i] = new Customer(barberia, "" + i);

        for(int i=0; i<customer_number ; i++)
            cliente[i].start();
    }
}
