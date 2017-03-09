/*
 * Copyright Anatoly Starostin (c) 2017.
 */

package treeton.core.util.combinator;

import org.apache.log4j.Logger;

import java.util.*;

@SuppressWarnings({"unchecked"})
public class CombinatorWithFrontOld<T> implements SortedEntriesListener<T>, Combinator<T> {
    private static final Logger logger = Logger.getLogger(CombinatorWithFrontOld.class);

    private static Entry FINAL_EDGE = new EntryWithStandardComparator(null, 0, null, null);
    private final Object LOCK = new Object();
    protected PriorityQueue<CombinationImpl<T>> combinationsFront;
    private String name;
    private List<SortedEntries<T>> sortedEntriesList;
    private CombinationImpl<T> currentCombination;
    private List<Entry<T>> edges;
    //private Combination<T> worstCombinationInsideFront;
    private Set<CombinationImpl> frontController;
    private Set<CombinationImpl> combinationsWithFixedDimensions;
    private int numberOfGeneratedCombinations = 0;
    private boolean started = false;
    private List<CombinatorListener<T>> listeners;

    public CombinatorWithFrontOld(SortedEntries<T>... sortedEntriesArray) {
        this.sortedEntriesList = new ArrayList<SortedEntries<T>>();
        this.sortedEntriesList.addAll(Arrays.asList(sortedEntriesArray));
        edges = new ArrayList<Entry<T>>();
        for (SortedEntries<T> entries : sortedEntriesArray) {
            entries.addListener(this);
            edges.add(null);
        }
        frontController = new HashSet<CombinationImpl>();
        combinationsWithFixedDimensions = new HashSet<CombinationImpl>();

        //worstCombinationInsideFront = null;
        listeners = new ArrayList<CombinatorListener<T>>();
    }

    public void addCombinatorListener(CombinatorListener<T> listener) {
        listeners.add(listener);
    }

    private void notifyChange() {
        for (CombinatorListener listener : listeners) {
            listener.combinationChanged(this);
        }
    }

    public void start() {
        started = true;
        next();
    }

    public Object getLOCK() {
        return LOCK;
    }

    public boolean next() {
        synchronized (LOCK) {
            if (!started) {
                return false;
            }

            if (combinationsFront == null) {
                //That means that till that moment one of the input sets was empty.
                //trying to create first combination.

                Entry<T>[] arr = new Entry[sortedEntriesList.size()];

                for (int i = 0; i < arr.length; i++) {
                    Entry<T> f = sortedEntriesList.get(i).getFirst();
                    if (f != null) {
                        arr[i] = f;
                    } else {
                        if (logger.isTraceEnabled())
                            logger.trace("Unable to create initial combination: one of the input sets is empty");
                        return false; //one of the inputs still is empty
                    }
                }

                currentCombination = new CombinationImpl<T>(arr, 0);
                combinationsFront = new PriorityQueue<CombinationImpl<T>>();

                if (logger.isTraceEnabled())
                    logger.trace("Initial combination succesfully created: " + currentCombination);
                numberOfGeneratedCombinations++;
            } else {
                unfold();
                currentCombination = combinationsFront.poll();
                if (logger.isTraceEnabled())
                    logger.trace("Took combination " + currentCombination + " from the top of the combinationsFront");

                if (currentCombination != null) {
                    numberOfGeneratedCombinations++;
                    frontController.remove(currentCombination);
                    if (currentCombination.getFixedDimension() != -1) {
                        combinationsWithFixedDimensions.remove(currentCombination);
                    } else { //that means that all additional lines have reached combinations front
                        for (Iterator<CombinationImpl> iterator = combinationsWithFixedDimensions.iterator(); iterator.hasNext(); ) {
                            CombinationImpl combination = iterator.next();
                            combination.setFixedDimension(-1);
                            for (int i = 0; i < edges.size(); i++) {
                                Entry<T> entry = edges.get(i);
                                Entry<T> e = combination.getValue(i);
                                if (entry != FINAL_EDGE && (entry == null || entry.compareTo(e) < 0)) {
                                    edges.set(i, e);
                                }
                            }

                            iterator.remove();
                        }
                    }

                    if (combinationsFront.isEmpty()) {
                        for (int i = 0; i < edges.size(); i++) {
                            edges.set(i, null);
                        }
                    }

                }
            }

            return currentCombination != null;
        }
    }

    public Collection<Combination> getCombinationsFront() {
        return new ArrayList<Combination>(combinationsFront);
    }

    private void unfold() {
        Entry<T>[] arr = currentCombination.getEntries();
        Entry<T>[] newarr = new Entry[sortedEntriesList.size()];
        CombinationImpl<T> combination = new CombinationImpl<T>(newarr, currentCombination.getDepth() + 1);
        System.arraycopy(arr, 0, newarr, 0, sortedEntriesList.size());

        int fixedIndex = currentCombination.getFixedDimension();
        Entry<T> fixedEntry = fixedIndex == -1 ? null : currentCombination.getValue(fixedIndex);

        for (int i = 0; i < sortedEntriesList.size(); i++) {
            if (i == fixedIndex)
                continue;

            Entry<T> e = arr[i];
            Entry<T> suc = e.getSuccessor();

            if (fixedEntry != null) {
                while (suc != null && suc.getCreationTime() > fixedEntry.getCreationTime()) {
                    //without this condition additional lines may intersect. After such intersection
                    //one of the lines will be interrupted by another => we will loose combinations.

                    suc = suc.getSuccessor();
                }
            }


            if (suc != null) {
                newarr[i] = suc;

                if (frontController.contains(combination)) {
                    newarr[i] = e;
                    continue;
                }

                if (i < sortedEntriesList.size() - 1) {
                    Entry<T>[] newarr1 = new Entry[sortedEntriesList.size()];
                    System.arraycopy(newarr, 0, newarr1, 0, sortedEntriesList.size());
                    CombinationImpl<T> combination1 = new CombinationImpl<T>(newarr1, currentCombination.getDepth() + 1);
                    combination1.setFixedDimension(currentCombination.getFixedDimension());
                    addToFront(combination1);
                    newarr[i] = e;
                } else {
                    combination.setFixedDimension(currentCombination.getFixedDimension());
                    //combination.resetNorm(); //На всякий пожарный
                    addToFront(combination);
                }
            } else {
                edges.set(i, FINAL_EDGE);
            }
        }
    }

    public Combination<T> getCurrentCombination() {
        return currentCombination;
    }

    public synchronized void entryAdded(Entry<T> e, SortedEntries<T> entries) {
        synchronized (LOCK) {
            if (!started)
                return;

            if (logger.isTraceEnabled())
                logger.trace("Handling entry addition. Stream " + sortedEntriesList.indexOf(entries) + ", id " + e.getId() + ", priority " + e.getPriority() + ".");

            if (combinationsFront == null) {
                if (logger.isTraceEnabled())
                    logger.trace("Trying to create initial combination");
                next();
                if (currentCombination != null) {
                    notifyChange();
                }
            } else {
                int idx = sortedEntriesList.indexOf(entries);

                Entry<T> edge = edges.get(idx);

                if (false && (edge != FINAL_EDGE && edge != null && edge.compareTo(e) < 0)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Nothing to do because we are outside of the combinations front");
                    }
                } else {

                    Entry<T>[] arr = new Entry[sortedEntriesList.size()];

                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = i == idx ? e : sortedEntriesList.get(i).getFirst();
                    }

                    int depth = 0;

                    Entry<T> en = entries.getFirst();

                    while (en != e) {
                        en = en.getSuccessor();
                        depth++;
                    }

                    CombinationImpl<T> combination = new CombinationImpl<T>(arr, depth);
                    combination.setFixedDimension(idx);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Combination " + combination + " created");
                    }

                    if (logger.isTraceEnabled()) {
                        logger.trace("Perfoming validation of current combination and combinations front ");
                    }

                    if (currentCombination == null || currentCombination.compareTo(combination) > 0) {
                        if (currentCombination != null) {
                            addToFront(currentCombination);
                        }
                        currentCombination = combination;
                        notifyChange();
                    } else {
                        addToFront(combination);
                    }
                }
            }
        }
    }

    private void addToFront(CombinationImpl<T> combination) {
        if (logger.isTraceEnabled())
            logger.trace("Adding combination " + combination + " to the combinationsFront, at the moment edges are  " + edges);

        if (combination.getFixedDimension() == -1) {
            for (int i = 0; i < edges.size(); i++) {
                Entry<T> entry = edges.get(i);
                Entry<T> e = combination.getValue(i);
                if (entry != FINAL_EDGE && (entry == null || entry.compareTo(e) < 0)) {
                    edges.set(i, e);
                }
            }
        }

        combinationsFront.add(combination);
        frontController.add(combination);
        if (combination.getFixedDimension() != -1) {
            combinationsWithFixedDimensions.add(combination);
        }
    }

    public String getStatistics() {
        return "front size is " + combinationsFront.size();
    }

    public List<SortedEntries<T>> getSortedEntriesList() {
        return sortedEntriesList;
    }

    public int getNumberOfGeneratedCombinations() {
        return numberOfGeneratedCombinations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void combinationUsed(Combination<T> combination) {
        if (combination == currentCombination) {
            next();
        }
    }
}