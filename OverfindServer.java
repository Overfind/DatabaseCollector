import EventCollector.EventCollector;
import java.util.Calendar;
import java.util.Date;


public class OverfindServer
{
    public static void main(String[] args)
    {
        System.out.println("-----Routine Started-----");
        
        Date sdate_start = null, sdate_end = null;
        Calendar cal;
        
        if( 2 <= args.length )
        {
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, Integer.parseInt(args[1]));
            sdate_start = cal.getTime();
        }
        if( 3 <= args.length )
        {
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, Integer.parseInt(args[2]));
            sdate_end = cal.getTime();
        }
        
        foo(sdate_start, sdate_end);
        
        
        System.out.println("-----Routine Ended-----");
        
        System.exit(0);
    }
    
    
    static void foo(Date s, Date e)
    {
        long time = System.currentTimeMillis();
        
        EventCollector ec = new EventCollector(s, e);
        ec.run();
        
        time = System.currentTimeMillis() - time;
        int seconds = (int) (time / 1000) % 60 ;
        int minutes = (int) ((time / (1000*60)) % 60);
        System.out.println("Execution time:\t" + minutes + " min " + seconds + " seconds.");
    }
}
