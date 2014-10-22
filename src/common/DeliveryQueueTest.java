package common;

public class DeliveryQueueTest {

	public static void main(String[] args) {
		
		DeliveryQueue queue = new DeliveryQueue(10);
        
		System.out.println("Start von addMessage:");
        for(int i = 0; i < 11; i++) {
        	Message message = new Message("test", String.valueOf(i), 60);
        	queue.addMessage(message);
        	System.out.println(message);
        }
        
        System.out.println("\nInhalt der MessageQueue: \n" + queue);

        System.out.println("\n\nStart von getMessage:");
        for(int i = 0; i < 11; i++) {
        	System.out.println(queue.getMessage("test"));
        }
	}

}
