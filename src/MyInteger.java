/**
 * Created by Вова on 04.11.2017.
 */



public class MyInteger {

    private Integer i;
    private Integer incremented = 0;
    private Integer decremented = 0;

    MyInteger(){
        i = 0;
    }

    public void inc(){
        i++;
    }

    public void dec(){
        i--;
    }

    public void plus(int a){ i += a;
        if(a > 0) incremented+=a;
        else if(a < 0) decremented-=a;
    }

    public void minus(int a){ i -= a;
            decremented++;
    }

    public Integer getInt(){
        return i;
    }

    public Integer getDecremented(){
        return decremented;
    }

    public Integer getIncremented(){
        return incremented;
    }


}
