package EventCollector;

import java.util.Date;

public class EventCollector implements Runnable 
{
    private Date s, e;
    
    public EventCollector(Date s, Date e)
    {
        this.s = s;
        this.e = e;
    }
    
    @Override
    public void run()
    {
        fetchEventLists();
    }
    
    private boolean fetchEventLists()
    {
        EventbriteFetcher ebFetcher = new EventbriteFetcher();
        ebFetcher.setStartDateRange(s, e);
        
        ebFetcher.fecthEventList();
        
        return true;
    }
}
