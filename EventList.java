package DatabaseHandler;

import java.util.ArrayList;
import java.util.Iterator;

public class EventList extends ArrayList<Event>
{
    public EventList()
    {
        super();
    }
    
    public EventList(int i)
    {
        super(i);
    }
    
    @Override
    public String toString()
    {
        String str = "";
        for (Iterator<Event> it = this.iterator(); it.hasNext();)
            str += it.next() + "\n";
        return str;
    }
}
