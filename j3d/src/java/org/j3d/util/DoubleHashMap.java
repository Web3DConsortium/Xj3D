/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Local imports
// None

/**
 * A hash map that uses primitive doubles for the key rather than objects.
 * <P>
 *
 * @author <a href="mailto:georg.rehfeld@gmx.de">Georg Rehfeld</a>
 * @version $Revision: 1.1 $
 * @param <V>
 */
public class DoubleHashMap<V>
{
    /** The hash table data. */
    private transient Entry<V>[] table;

    /** The total number of entries in the hash table. */
    private transient int count = 0;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */
    private int threshold;

    /** The load factor for the hashtable. */
    private float loadFactor;

    /**
     * The number of times this Hashtable has been structurally modified
     * Structural modifications are those that change the number of entries in
     * the Hashtable or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the Hashtable fail-fast.  (See ConcurrentModificationException).
     */
    private transient int modCount = 0;

    /** Cache of the entry instances to prevent excessive object creation */
    private List<Entry<V>> entryCache;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * table.
     */
    private static class Entry<V>
    {
        int hash;
        double key;
        V value;
        Entry<V> next;

        /**
         * Create a new default entry with nothing set.
         */
        protected Entry()
        {
        }

        /**
         * Create a new entry with the given values.
         *
         * @param hash The code used to hash the object with
         * @param key The key used to enter this in the table
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected Entry(int hash, double key, V value, Entry<V> next)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * Convenience method to set the entry with the given values.
         *
         * @param hash The code used to hash the object with
         * @param key The key used to enter this in the table
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected void set(int hash, double key, V value, Entry<V> next)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <tt>20</tt> and <tt>0.75</tt> respectively.
     */
    public DoubleHashMap()
    {
        this(20, 0.75f);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param  initialCapacity the initial capacity of the hashtable.
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public DoubleHashMap(int initialCapacity)
    {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hashtable.
     * @param loadFactor the load factor of the hashtable.
     * @throws IllegalArgumentException  if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    @SuppressWarnings("unchecked")
    public DoubleHashMap(int initialCapacity, float loadFactor)
    {
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

        entryCache = new ArrayList<>(initialCapacity);
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */
    public int size()
    {
        return count;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the <code>containsKey</code>
     * method.<p>
     *
     * Note that this method is identical in functionality to containsValue,
     * (which is part of the Map interface in the collections framework).
     *
     * @param      value   a value to search for.
     * @return     <code>true</code> if and only if some key maps to the
     *             <code>value</code> argument in this hashtable as
     *             determined by the <tt>equals</tt> method;
     *             <code>false</code> otherwise.
     * @throws  NullPointerException  if the value is <code>null</code>.
     * @see        #containsKey(double)
     * @see        #containsValue(Object)
     * @see        java.util.Map
     */
    public boolean contains(V value)
    {
        if (value == null)
        {
            throw new NullPointerException("value object may not be null!");
        }

        Entry<V>[] tab = table;
        for (int i = tab.length ; i-- > 0 ;)
        {
            for (Entry<V> e = tab[i] ; e != null ; e = e.next)
            {
                if (e.value.equals(value))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this HashMap maps one or more keys to this value.<p>
     *
     * Note that this method is identical in functionality to contains
     * (which predates the Map interface).
     *
     * @param value value whose presence in this HashMap is to be tested.
     * @return
     * @see    java.util.Map
     * @since JDK1.2
     */
    public boolean containsValue(V value)
    {
        return contains(value);
    }

    /**
     * Tests if the specified object is a key in this hashtable.
     *
     * @param  key  possible key.
     * @return <code>true</code> if and only if the specified object is a
     *    key in this hashtable, as determined by the <tt>equals</tt>
     *    method; <code>false</code> otherwise.
     * @see #contains(Object)
     */
    public boolean containsKey(double key)
    {
        Entry<V>[] tab = table;
        long bits = Double.doubleToLongBits(key);
        int hash = (int)(bits ^ (bits >>> 32));
        int index = (hash & 0x7FFF_FFFF) % tab.length;
        for (Entry<V> e = tab[index] ; e != null ; e = e.next)
        {
            if (e.hash == hash && e.key == key)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped in this map.
     *
     * @param   key   a key in the hashtable.
     * @return  the value to which the key is mapped in this hashtable;
     *          <code>null</code> if the key is not mapped to any value in
     *          this hashtable.
     * @see     #put(double, Object)
     */
    public V get(double key)
    {
        Entry<V>[] tab = table;
        long bits = Double.doubleToLongBits(key);
        int hash = (int)(bits ^ (bits >>> 32));
        int index = (hash & 0x7FFF_FFFF) % tab.length;
        for (Entry<V> e = tab[index] ; e != null ; e = e.next)
        {
            if (e.hash == hash && e.key == key)
            {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. The key cannot be
     * <code>null</code>. <p>
     *
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     *
     * @param key     the hashtable key.
     * @param value   the value.
     * @return the previous value of the specified key in this hashtable,
     *         or <code>null</code> if it did not have one.
     * @see     #get(double)
     */
    public V put(double key, V value)
    {
        // Makes sure the key is not already in the hashtable.
        Entry<V>[] tab = table;
        long bits = Double.doubleToLongBits(key);
        int hash = (int)(bits ^ (bits >>> 32));
        int index = (hash & 0x7FFF_FFFF) % tab.length;
        for (Entry<V> e = tab[index] ; e != null ; e = e.next)
        {
            if (e.hash == hash && e.key == key)
            {
                V old = e.value;
                e.value = value;
                return old;
            }
        }

        modCount++;
        if (count >= threshold)
        {
            // Rehash the table if the threshold is exceeded
            rehash();

            tab = table;
            index = (hash & 0x7FFF_FFFF) % tab.length;
        }

        // Creates the new entry.
        Entry<V> e = getNewEntry();
        e.set(hash, key, value, tab[index]);

        tab[index] = e;
        count++;
        return null;
    }

    /**
     * Removes the key (and its corresponding value) from this
     * hashtable. This method does nothing if the key is not in the hashtable.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this hashtable,
     *          or <code>null</code> if the key did not have a mapping.
     */
    public V remove(double key)
    {
        Entry<V>[] tab = table;
        long bits = Double.doubleToLongBits(key);
        int hash = (int)(bits ^ (bits >>> 32));
        int index = (hash & 0x7FFF_FFFF) % tab.length;
        for (Entry<V> e = tab[index], prev = null ; e != null ; prev = e, e = e.next)
        {
            if (e.hash == hash && e.key == key)
            {
                modCount++;
                if (prev != null)
                {
                    prev.next = e.next;
                } else
                {
                    tab[index] = e.next;
                }
                count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }

    /**
     * Clears this hashtable so that it contains no keys.
     */
    public synchronized void clear()
    {
        if(count == 0)
            return;

        Entry<V>[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry<V> e = tab[index];

            if(e == null)
                continue;

            while(e.next != null)
            {
                e.value = null;
                releaseEntry(e);

                Entry<V> n = e.next;
                e.next = null;
                e = n;
            }

            tab[index] = null;
        }
        count = 0;
    }

    /**
     * Returns an array with all keys. The order of keys is unspecified.
     *
     * @return  the array with the keys
     */
    public double[] keySet()
    {
        double result[] = new double[count];
        int i = 0;

        Entry<V>[] tab = table;
        for (int index = tab.length ; index-- > 0 ;)
        {
            for (Entry<V> e = tab[index] ; e != null ; e = e.next)
            {
                result[i++] = e.key;
            }
        }

        return result;
    }

    /**
     * Returns a sorted array with all keys. The keys are sorted ascending.
     *
     * @return  the sorted array with the keys
     */
    public double[] keysSorted()
    {
        double result[] = keySet();
        Arrays.sort(result);

        return result;
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     */
    private void rehash()
    {
        int oldCapacity = table.length;
        Entry<V>[] oldMap = table;

        int newCapacity = oldCapacity * 2 + 1;

        @SuppressWarnings("unchecked")
        Entry<V>[] newMap = new Entry[newCapacity];

        modCount++;
        threshold = (int)(newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;)
        {
            for (Entry<V> old = oldMap[i] ; old != null ; )
            {
                Entry<V> e = old;
                old = old.next;

                int index = (e.hash & 0x7FFF_FFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * Grab a new entry. Check the cache first to see if one is available. If
     * not, create a new instance.
     *
     * @return An instance of the Entry
     */
    private Entry<V> getNewEntry()
    {
        Entry<V> ret_val;

        int size = entryCache.size();
        if(size == 0)
            ret_val = new Entry<>();
        else
            ret_val = entryCache.remove(size - 1);

        return ret_val;
    }

    /**
     * Release an entry back into the cache.
     *
     * @param e The entry to put into the cache
     */
    private void releaseEntry(Entry<V> e)
    {
        entryCache.add(e);
    }
}
