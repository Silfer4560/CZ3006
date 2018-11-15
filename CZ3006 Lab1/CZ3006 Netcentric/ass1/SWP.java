/*===============================================================
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         				 *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Done by: Yeo Tee Yang										 *
 *           U1620078A											 *
 *			 SSP3												 *
 *																 *
 ===============================================================*/
import java.util.Timer;
import java.util.TimerTask;
public class SWP {

/*========================================================================
 the following are provided, do not change them!!
 ========================================================================*/
   //the following are protocol constants.
   public static final int MAX_SEQ = 7; 
   public static final int NR_BUFS = (MAX_SEQ + 1)/2;

   // the following are protocol variables
   private int oldest_frame = 0;
   private PEvent event = new PEvent();  
   private Packet out_buf[] = new Packet[NR_BUFS];

   //the following are used for simulation purpose only
   private SWE swe = null;
   private String sid = null;  

   //Constructor
   public SWP(SWE sw, String s){
      swe = sw;
      sid = s;
   }

   //the following methods are all protocol related
   private void init(){
      for (int i = 0; i < NR_BUFS; i++){
	   out_buf[i] = new Packet();
      }
   }

   private void wait_for_event(PEvent e){
      swe.wait_for_event(e); //may be blocked
      oldest_frame = e.seq;  //set timeout frame seq
   }

   private void enable_network_layer(int nr_of_bufs) {
   //network layer is permitted to send if credit is available
	swe.grant_credit(nr_of_bufs);
   }

   private void from_network_layer(Packet p) {
      swe.from_network_layer(p);
   }

   private void to_network_layer(Packet packet) {
	swe.to_network_layer(packet);
   }

   private void to_physical_layer(PFrame fm)  {
      System.out.println("SWP: Sending frame: seq = " + fm.seq + 
			    " ack = " + fm.ack + " kind = " + 
			    PFrame.KIND[fm.kind] + " info = " + fm.info.data );
      System.out.flush();
      swe.to_physical_layer(fm);
   }

   private void from_physical_layer(PFrame fm) {
      PFrame fm1 = swe.from_physical_layer(); 
	fm.kind = fm1.kind;
	fm.seq = fm1.seq; 
	fm.ack = fm1.ack;
	fm.info = fm1.info;
   }


/*===========================================================================
 	implement your Protocol Variables and Methods below: 
 ==========================================================================*/
   static boolean no_nak = true;
   public boolean between(int a, int b, int c) { // returns true if A<=b<c circularly
	return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
   }
   public void send_frame(int frame_type, int frame_number, int frame_expected, Packet buffer[]){
	   PFrame newFrame = new PFrame(); //create a new frame to be sent
	   newFrame.kind = frame_type; // set the type of frame
	   if(frame_type == PFrame.DATA){ //take data from buffer
		   newFrame.info = buffer[frame_number % NR_BUFS];
	   }
	   newFrame.seq = frame_number;
	   newFrame.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);
	   if (frame_type == PFrame.NAK){//limit the amount of NAK per frame to 1
		   no_nak = false;
	   }
	   to_physical_layer(newFrame);
	   if (frame_type == PFrame.DATA){
		   start_timer(frame_number);
	   }
	   stop_ack_timer();
   }
   public int increment(int number){
	   number++;
	   if (number > MAX_SEQ){
		   number = 0;
	   }
	   return number;
   }
   public void protocol6() {
	   /*variables to be used*/
	   int expected_acknowledgement = 0; //lower edge of sender window
	   int next_frame_to_send = 0; //upper edge of sender window
	   int frame_expected = 0;//lower edge of receiver window
	   int over_boundary = NR_BUFS; //upper edge of receiver window + 1
	   int numbuffered = 0;
	   
	   PFrame incoming_frame = new PFrame();
	   Packet in_buffer[] = new Packet[NR_BUFS];  //array of packets that acts as a buffer
	   boolean packet_arrived[] = new boolean[NR_BUFS]; //boolean array for containing info as to whether the packet has arrived
	   enable_network_layer(NR_BUFS);//initialize network layer
	   for (int i = 0; i < NR_BUFS; i++) {//initialize the array
		   packet_arrived[i] = false;//set boolean values to false
		   in_buffer[i] = new Packet();//add the packets to buffer
		   
	   }
       init();
       
	while(true) {	
         wait_for_event(event);
	   switch(event.type) {//5 kinds of events: network layer ready, frame arrival, checksum error, timeout, ack timeout
	      case (PEvent.NETWORK_LAYER_READY):
		  numbuffered++;//expand window
	      from_network_layer(out_buf[next_frame_to_send % NR_BUFS]); //fetch the new packet
	      send_frame(PFrame.DATA, next_frame_to_send, frame_expected, out_buf);//send the new frame
	      next_frame_to_send = increment(next_frame_to_send);//increment sequence number
                   break; 
	      case (PEvent.FRAME_ARRIVAL ):// when a frame has arrived
	    	  from_physical_layer(incoming_frame);//take the incoming frame from physical layer
	      if(incoming_frame.kind == PFrame.DATA) { //if incoming frame is data frame
	    	  if((incoming_frame.seq != frame_expected) && no_nak) {
	    		  send_frame(PFrame.NAK, 0, frame_expected, out_buf);// send a NAK
	    	  }
	    	  else {
	    		  start_ack_timer();//begin timer for the acknowledgement
	    	  }

	    	  if(between(frame_expected, incoming_frame.seq, over_boundary) && (packet_arrived[incoming_frame.seq % NR_BUFS] == false)) {
	    		  packet_arrived[incoming_frame.seq % NR_BUFS] = true;
	    		  in_buffer[incoming_frame.seq % NR_BUFS]= incoming_frame.info;
	    		  while(packet_arrived[frame_expected % NR_BUFS]) { //pass frames to network layer and move the window forward
	    			  to_network_layer(in_buffer[frame_expected % NR_BUFS]);
	    			  no_nak = true;
	    			  packet_arrived[frame_expected % NR_BUFS] = false;
	    			  frame_expected = increment(frame_expected);// move lower edge of reciever window
	    			  over_boundary = increment(over_boundary);// move the upper edge of reciever window
	    			  start_ack_timer(); //see if seperate ack timer is needed
	    		  }
	    	  }
	      }
	      if((incoming_frame.kind == PFrame.NAK) && between(expected_acknowledgement, (incoming_frame.ack + 1 % (MAX_SEQ + 1)), next_frame_to_send)){ // if the incoming frame is NAK 
	    	  send_frame(PFrame.DATA,(incoming_frame.ack+1) % (MAX_SEQ + 1), frame_expected, out_buf);//resend the frame that had a previous error
	      }
	      while(between(expected_acknowledgement, incoming_frame.ack,next_frame_to_send)) {
			  numbuffered--; //for piggybacked ack
	    	  stop_timer(expected_acknowledgement);//if frame arrived intact
	    	  expected_acknowledgement = increment(expected_acknowledgement);//advance sender lower edge window
	    	  enable_network_layer(1);//free a buffer slot
	      }
		   break;	   
              case (PEvent.CKSUM_ERR): //when there is a checksum error, send a negative acknowledgement
            	  if(no_nak) {
            		  send_frame(PFrame.NAK,0,frame_expected, out_buf);
            	  }
      	           break;  
              case (PEvent.TIMEOUT): //when there is a timeout, resend data
            	  send_frame(PFrame.DATA, oldest_frame, frame_expected, out_buf);
	           break; 
	      case (PEvent.ACK_TIMEOUT)://when there is an acknowledgement timeout, send acknowledgement frame
	    	  send_frame(PFrame.ACK, 0, frame_expected, out_buf);
                   break; 
            default: 
		   System.out.println("SWP: undefined event type = " 
                                       + event.type); //event error
		   System.out.flush(); //ensure data written gets out to file
	   }
      }      
   }

 /* Note: when start_timer() and stop_timer() are called, 
    the "seq" parameter must be the sequence number, rather 
    than the index of the timer array, 
    of the frame associated with this timer, 
   */
   private Timer timer[] = new Timer [NR_BUFS];   //use an array to store timers for different sequence numbers
   private Timer ack_timer = new Timer(); //timer for acknowledgements
   
   public class FrameTimeoutTask extends TimerTask{//class created for frame timeouts
	   int frame_sequence_number;
	   public FrameTimeoutTask(int frame_sequence_number){
		   this.frame_sequence_number = frame_sequence_number;
	   }
	   public void run(){
		   swe.generate_timeout_event(frame_sequence_number);
	   }
   }
    public class AckTimeout extends TimerTask{//class created for acknowledgement timeouts
	   public void run(){
		   swe.generate_acktimeout_event();
	   }
   }
   private void start_timer(int sequence_number) {
     stop_timer(sequence_number);//has to stop the previous timer of the same sequence number
     timer[sequence_number % NR_BUFS] = new Timer(); // start a new timer of using the sequence number
     timer[sequence_number % NR_BUFS].schedule(new FrameTimeoutTask(sequence_number), 50);//set timeout delay for new timer
   }

   private void stop_timer(int sequence_number) {
	   if ( timer[sequence_number % NR_BUFS] != null) { //if there is a previous timer for sequence number
		   timer[sequence_number % NR_BUFS].cancel(); //stop timer
		   timer[sequence_number % NR_BUFS] = null; //remove value
	   }
   }
   
   private void start_ack_timer() {
      stop_ack_timer(); //stop any previous timers
      ack_timer = new Timer(); //create new acknowledgement timer
      ack_timer.schedule(new AckTimeout(), 100); // set the timeout duration for the new timer to 100ms
   }

   private void stop_ack_timer() {
     if (ack_timer != null) { //if there is an acknowledgement timer 
    	 ack_timer.cancel(); //stop timer
    	 ack_timer = null; //remove value
     }
   }

}//End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
     swe.generate_acktimeout_event(), or
     swe.generate_timeout_event(seqnr).
*/