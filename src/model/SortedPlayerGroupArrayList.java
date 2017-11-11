package model;

import java.util.*;

/**
 * Overloaded array list that automatically keeps playerGroups sorted
 * @param <E> If E is not PlayerGroup, then this will act as a normal array list
 */
public class SortedPlayerGroupArrayList<E> extends ArrayList<E> {

    public SortedPlayerGroupArrayList(Collection<? extends E> c){
        super(c);
        Collections.sort(this, getComparator());
    }

    public SortedPlayerGroupArrayList(){
        super();
    }

    @Override
    public boolean add(E eObject) {
        super.add(eObject);
        Collections.sort(this, getComparator());
        return true;
    }

    private Comparator getComparator() {
        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof PlayerGroup && o2 instanceof PlayerGroup) {
                    return ((PlayerGroup) o1).getTotalScore() - ((PlayerGroup) o2).getTotalScore();
                } else {
                    return 0;
                }
            }
        };
    }
}
