package Debug;

public class Debugger 
{
    public static enum DEBUG {};
    public static enum DATABASE_HANDLER {QUERY};
    public static enum EVENT_COLLECTOR {URL, PAGINATOR};
    
    
    private boolean debugOutput = false;
    private boolean databaseHandlerOutput = false;
    private boolean eventCollectorOutput = true;
    
    
    private static Debugger debugger;
    
    private Debugger()
    {
        // TODO get from config
    }
    
    /**
     * Singleton Getter
     * @return Debugger
     */
    public static Debugger getInstance()
    {
        if( debugger == null )
            debugger = new Debugger();
        return debugger;
    }
    
    public void printDebug(DEBUG debug, String str)
    {
        if( !debugOutput )
            return;
    }
    
    public void printDatabaseHandler(DATABASE_HANDLER debug, String str)
    {
        if( !databaseHandlerOutput )
            return;
        
        if( debug == DATABASE_HANDLER.QUERY )
            System.out.println("DATABASE - QUERY:\t" + str);
    }
    
    public void printEventCollector(EVENT_COLLECTOR debug, String str)
    {
        if( !eventCollectorOutput )
            return;
        
        if( debug == EVENT_COLLECTOR.URL )
            System.out.println("EVENT_COLLECTOR - URL:\t" + str);
        else if( debug == EVENT_COLLECTOR.PAGINATOR )
            System.out.println("EVENT_COLLECTOR - PAGINATOR:\t" + str);
    }
}
