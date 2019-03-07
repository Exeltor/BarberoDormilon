package listaDeBloqueo;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Problema Barbero-Dormilon
 * 
 * Se considera una barberia con un unico barbero B, una silla de barbero,
 * y n sillas para los clientes que esten esperando para su turno para un corte de pelo.
 * 
 * El barbero duerme en su silla cuando no hay clientes presentes.
 * Al entrar, un cliente despierta al barbero en el caso que este durmiendo, o si el 
 * barbero esta ocupado con un corte de pelo, se sienta en una silla en la sala de espera.
 * Si todas las sillas de la sala de espera estan ocupadas, el cliente se va.
 */
public class BlockingQueueSleepingBarber extends Thread {
    public static final int CHAIRS = 5;

    public static final long BARBER_TIME = 5000;

    private static final long CUSTOMER_TIME = 2000;
    
    private static final long CUSTOMER_WAITING_TIME = 500;

    public static final long OFFICE_CLOSE = BARBER_TIME * 2;

    public static BlockingQueue queue = new ArrayBlockingQueue(CHAIRS);

    class Customer extends Thread {
        int iD;
        boolean notCut = true;
        BlockingQueue queue = null;

        public Customer(int i, BlockingQueue queue) {
            iD = i;
            this.queue = queue;
        }

        public void run() {
        	//Si el cliente no ha recibido un corte de pelo se mete en la cola. Si no hay asientos disponibles, el cliente espera CUSTOMER_WAITING_TIME
        	//Si no hay asientos disponibles despues de la espera, se va.
            while (true) {
                try {
                	if(this.queue.offer(this.iD, CUSTOMER_WAITING_TIME, TimeUnit.MILLISECONDS)) {
                		this.getHaircut();
                	} else {
                		System.out.println("No hay asientos disponibles. Cliente "
                                + this.iD + " se ha ido de la barberia");
                	}
                	
                	System.out.println("Clientes en cola: " + queue.size());
                    //this.queue.add(this.iD);
                } catch (IllegalStateException | InterruptedException e) {
                    System.out.println("No hay asientos disponibles. Cliente "
                            + this.iD + " se ha ido de la barberia");
                }
                break;
            }
        }

        //Coger silla de espera.
        public void getHaircut() {
            System.out.println("Cliente " + this.iD + " coge una silla");
        }

    }

    class Barber extends Thread {
        BlockingQueue queue = null;
        public Barber(BlockingQueue queue) {
            this.queue = queue;
        }

        public void run() {
            while (true) {
                try {
                    Integer i = (Integer) this.queue.poll(OFFICE_CLOSE, TimeUnit.MILLISECONDS);
                    if (i==null) {
                    	closeShop(); //El barbero ha dormido demasiado tiempo (OFFICE_CLOSE), no hay mas clientes en la cola - cerrar tienda.
                    	break;
                    }
                    this.cutHair(i); //Cortando pelo

                } catch (InterruptedException e) {

                }
            }
        }

        //Cortar pelo
        public void cutHair(Integer i) {
            System.out.println("El barbero esta cortando el pelo al cliente #" + i);
            try {
                sleep(BARBER_TIME);
            } catch (InterruptedException ex) {
            }
        }
        
        public void closeShop() {
        	System.out.println("El barbero ha esperado demasiado tiempo. Cerramos barberia.");
        }
    }

    public static void main(String args[]) {

        BlockingQueueSleepingBarber barberShop = new BlockingQueueSleepingBarber();
        barberShop.start(); //Comenzar hilo de la cola de bloqueo.
    }

    public void run() {
        Barber barbero = new Barber(BlockingQueueSleepingBarber.queue);
        barbero.start();

        //Crear nuevos clientes.
        for (int i = 1; i < 16; i++) {
            Customer aCustomer = new Customer(i, BlockingQueueSleepingBarber.queue);
            aCustomer.start();
            try {
                sleep(CUSTOMER_TIME);
            } catch (InterruptedException ex) {};
        }
    }
}