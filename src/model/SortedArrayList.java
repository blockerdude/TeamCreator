package model;

import java.util.*;

public class SortedArrayList<E> extends ArrayList<E> {

    public SortedArrayList(Collection<? extends E> c){
        super(c);
        Collections.sort(this, getComparator());
    }

    public SortedArrayList(){
        super();
    }

    @Override
    public boolean add(E eObject) {
        super.add(eObject);
        Collections.sort(this, getComparator());
        return true;
    }

    private Comparator getComparator() {
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof PlayerGroup && o2 instanceof PlayerGroup) {
                    return ((PlayerGroup) o1).getTotalScore() - ((PlayerGroup) o2).getTotalScore();
                } else {
                    return 0;
                }
            }
        };
        return comparator;
    }
}
