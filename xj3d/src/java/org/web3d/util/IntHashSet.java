/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External imports
// None

// Local imports
// None

/**
 * A custom IntHashSet implementation specific to int primitive data.
 * <p>
 *
 * This implementation is designed for realtime work and in particular with
 * the goal of absolute minimum garbage generation. The standard implementation
 * in java.util generates excessive amounts of garbage and is unsuitable for
 * the task.
 * <p>
 *
 * The implementation does not have a backing class and the internals are based
 * on the hashing code in IntHashMap.  The method signature is almost the same as
 * java.util.IntHashSet, except we leave out garbage generating methods like iterator().
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 * @param <E> the element to be contained in this Set
 */
public class IntHashSet<E> {

    /** The hash table data.*/
    private Entry[] table;

    /** The total number of entries in the hash table. */
    private int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     */
    private int threshold;

    /** The load factor for the hashtable. */
    private float loadFactor;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * table.
     */
    private static class Entry {
        int hash;
        int value;
        Entry next;

        /**
         * Create a new entry with the given values.
         *
         * @param hash The code used to hash the object with
         * @param key The key used to enter this in the table
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected Entry(int hash, int value, Entry next)
        {
            this.hash = hash;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * Constructs a new, empty set; the backing <code>HashMap</code> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public IntHashSet() {
        this(20, 0.75f);
    }

    /**
     * Constructs a new, empty set; the backing <code>HashMap</code> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash map.
     * @param loadFactor the load factor of the hash map.
     * @throws IllegalArgumentException if the initial capacity is less
     *    than zero, or if the load factor is nonpositive.
     */
    public IntHashSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

        if (initialCapacity == 0)
            initialCapacity = 1;

        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing <code>HashMap</code> instance has
     * the specified initial capacity and default load factor, which is
     * <code>0.75</code>.
     *
     * @param initialCapacity   the initial capacity of the hash table.
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public IntHashSet(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set
     */
    public int size() {
        return count;
    }

    /**
     * Check to see if this set contains elements.
     *
     * @return true if this set contains no elements.
     */
    public boolean isEmpty() {
        return count==0;
    }

    /**
     * Returns true if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return true if this set contains the specified element.
     */
    public boolean contains(int o) {
        int hash = o;

        Entry[] tab = table;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next) {
            if (e.value == o) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return true if the set did not already contain the specified
     * element.
     */
    public boolean add(int o) {
        // Makes sure the key is not already in the hashtable.

        int hash = o;
        Entry[] tab = table;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next) {
            if (e.value == o) {
                return false;
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();
            tab=table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }
        // Creates the new entry.
        Entry e = new Entry(hash, o, tab[index]);
        tab[index] = e;
        count++;
        return true;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return true if the set contained the specified element.
     */
    public boolean remove(int o) {
        Entry tab[] = table;
        int hash = o;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
            if (e.value == o) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;

                return true;
            }
        }

        return false;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        Entry tab[] = table;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }

    /**
     * Adds all of the elements in the specified collection to this set.
     * The behavior of this operation is undefined if the specified collection
     * is modified while the operation is in progress.
     * <p>
     * This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.
     *
     * @param c collection whose elements are to be added to this collection.
     * @return true if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if this collection does not
     *         support the <code>addAll</code> method.
     * @throws NullPointerException if the specified collection is null.
     */
    public boolean addAll(int[] c) {
        boolean modified = false;

        for(int i=0; i < c.length; i++) {
            if (add(c[i])) {
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Removes from this set all of its elements that are contained in
     * the specified collection.
     * <p>
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <code>remove</code> method.<p>
     *
     * @param c elements to be removed from this set.
     * @return true if this collection changed as a result of the call.
     * @throws UnsupportedOperationException if the <code>removeAll</code> method
     *         is not supported by this collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(int)
     * @see #contains(int)
     */
    public boolean removeAll(int[] c) {
        boolean modified = false;

        for(int i=0; i < c.length; i++) {
            if (remove(c[i])) {
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.  The returned array will be "safe" in that no references to
     * it are maintained by the collection.  (In other words, this method must
     * allocate a new array even if the collection is backed by an Array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This implementation allocates the array to be returned, and iterates
     * over the elements in the collection, storing each object reference in
     * the next consecutive element of the array, starting with element 0.
     *
     * @return an array containing all of the elements in this collection.
     */
    public int[] toArray() {

        int[] ret_val = new int[count];
        int cnt=0;
        for (Entry e : table) {
            while (e!=null)
            {
                ret_val[cnt++]=e.value;
                e=e.next;
            }
        }

        return ret_val;
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.<p>
     *
     * If the collection fits in the specified array with room to spare (i.e.,
     * the array has more elements than the collection), the element in the
     * array immediately following the end of the collection is set to
     * <code>null</code>.  This is useful in determining the length of the
     * collection <i>only</i> if the caller knows that the collection does
     * not contain any <code>null</code> elements.)<p>
     *
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. <p>
     *
     * This implementation checks if the array is large enough to contain the
     * collection; if not, it allocates a new array of the correct size and
     * type (using reflection).  Then, it iterates over the collection,
     * storing each object reference in the next consecutive element of the
     * array, starting with element 0.  If the array is larger than the
     * collection, a <code>null</code> is stored in the first location after the
     * end of the collection.
     *
     * @param array the array into which the elements of the set are to
     *     be stored, if it is big enough; otherwise, a new array of the
     *     same runtime type is allocated for this purpose.
     * @return an array containing the elements of the collection.
     * @throws NullPointerException if the specified array is <code>null</code>.
     * @throws ArrayStoreException if the runtime type of the specified array
     *     is not a supertype of the runtime type of every element in this
     *     collection.
     */
    public int[] toArray(int[] array) {
        int size = count;

        if(array.length < size) {
            array = new int[size];
        }

        int cnt=0;
        int[] result = array;
        for (Entry e : table) {
            while (e!=null)
            {
                result[cnt++]=e.value;
                e=e.next;
            }
        }

        // TODO: Shouldn't this be returning result instead of array??
        return array;
    }

    /**
     * Compares the specified object with this set for equality.  Returns
     * true if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.
     *
     * This implementation first checks if the specified object is this
     * set; if so it returns true.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it it returns false.  If so, it returns
     * <code>containsAll((Collection) o)</code>.
     *
     * @param o Object to be compared for equality with this set.
     * @return true if the specified object is equal to this set.
     */
    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;

        if(!(o instanceof IntHashSet))
            return false;

        IntHashSet hs = (IntHashSet)o;

        if(hs.size() != size())
            return false;

        boolean ret_val = true;

        for (Entry e : table) {
            while (e != null) {
                if (!hs.contains(e.value)) {
                    ret_val = false;
                    break;
                }
                e = e.next;
            }
        }

        return ret_val;
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set.
     * This ensures that <code>s1.equals(s2)</code> implies that
     * <code>s1.hashCode()==s2.hashCode()</code> for any two sets <code>s1</code>
     * and <code>s2</code>, as required by the general contract of
     * Object.hashCode.<p>
     *
     * This implementation enumerates over the set, calling the
     * <code>hashCode</code> method on each element in the collection, and
     * adding up the results.
     *
     * @return the hash code value for this set.
     */
    @Override
    public int hashCode() {
        int h = 0;

        for (Entry e : table) {
            while (e!=null) {
                h += e.value;
                e = e.next;
            }
        }
        return h;
    }

    /**
     * Returns a string representation of this set.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<code>"[]"</code>).  Adjacent elements are separated by the characters
     * <code>", "</code> (comma and space).  Elements are converted to strings as
     * by <code>String.valueOf(Object)</code>.<p>
     *
     * This implementation creates an empty string buffer, appends a left
     * square bracket, and iterates over the collection appending the string
     * representation of each element in turn.  After appending each element
     * except the last, the string <code>", "</code> is appended.  Finally a right
     * bracket is appended.  A string is obtained from the string buffer, and
     * returned.
     *
     * @return a string representation of this collection.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int cnt=0;
        for (Entry e : table) {
            while (e!=null)
            {
                buf.append(e.value);

                if(++cnt < count)
                    buf.append(", ");

                e=e.next;
            }
        }

        buf.append("]");
        return buf.toString();
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     */
    private void rehash() {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        threshold = (int)(newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry old = oldMap[i] ; old != null ; ) {
                Entry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }
}
