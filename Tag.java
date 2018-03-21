package DatabaseHandler;

import org.neo4j.driver.v1.types.Node;

public class Tag
{
    int id;
    String tagName;
    
    public Tag(int id, Node node)
    {
        this.id = id;
        
        node.asMap().entrySet().forEach((pair) ->
        {
            switch( pair.getKey() )
            {
                case "tagName":
                    this.tagName = (String) pair.getValue();
                    break;
                default:
                    break;
            }
        });
    }
    
    @Override
    public String toString()
    {
        return "id:       " + id
           + "\nTag Name: " + tagName;
    }
}