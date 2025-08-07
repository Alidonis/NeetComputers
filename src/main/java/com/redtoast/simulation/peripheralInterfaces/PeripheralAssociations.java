package com.redtoast.simulation.peripheralInterfaces;

import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;

/**
* Allows you to associate certain object types with both {@link PeripheralConsumer}'s and {@link PeripheralProvider}'s
 * <p>These associations are automatically used when locating an assets peripheral capability and can be used to give blocks and items peripheral ability without modifying the class at all</p>
*/
public class PeripheralAssociations {

    private static class DualAssociator implements PeripheralAssociator<Object, Object> {
        public AssociatedPeripheralConsumerConstructor<?> consumer;
        public AssociatedPeripheralProviderConstructor<?> provider;
        public DualAssociator(AssociatedPeripheralConsumerConstructor<?> consumer, AssociatedPeripheralProviderConstructor<?> provider){
            this.consumer = consumer;
            this.provider = provider;
        }

        @Override
        public Object onConnection(Object source) {
            return null;
        }
    }

    private static final Hashtable<Class<?>, PeripheralAssociator<?, ?>> associationMap = new Hashtable<>();

    /**
     * Establishes an association between a class and consumer or overwrites a pre-existing one
     * @param clazz the class of the object your associating the consumer too
     * @param consumer the consumer being associated with the class
     */
    public static <T> void createAssociation(Class<T> clazz, AssociatedPeripheralConsumerConstructor<T> consumer){
        if (consumer==null) return;
        if (!associationMap.containsKey(clazz)){
            associationMap.put(clazz, consumer);
        }else{
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof DualAssociator dualAccesser){
                dualAccesser.consumer = consumer;
                return;
            }
            if (type instanceof AssociatedPeripheralProviderConstructor<?> provider){
                associationMap.put(clazz, new DualAssociator(consumer, provider));
                return;
            }
            associationMap.put(clazz, consumer);
        }
    }

    /**
     * Establishes an association between a class and provider or overwrites a pre-existing one
     * @param clazz the class of the object your associating the provider too
     * @param provider the provider being associated with the class
     */
    public static <T> void createAssociation(Class<T> clazz, AssociatedPeripheralProviderConstructor<T> provider){
        if (provider==null) return;
        if (!associationMap.containsKey(clazz)){
            associationMap.put(clazz, provider);
        }else{
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof DualAssociator dualAccesser){
                dualAccesser.provider = provider;
                return;
            }
            if (type instanceof AssociatedPeripheralConsumerConstructor<?> consumer){
                associationMap.put(clazz, new DualAssociator(consumer, provider));
                return;
            }
            associationMap.put(clazz, provider);
        }
    }

    /**
     * Tests if the class has an associated consumer
     * @param clazz the class of the object your checking for associations
     */
    public static boolean hasConsumer(Class<?> clazz){
        if (associationMap.containsKey(clazz)){
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            return type instanceof AssociatedPeripheralConsumerConstructor || type instanceof DualAssociator;
        }
        return false;
    }

    /**
     * Tests if the class has an associated provider
     * @param clazz the class of the object your checking for associations
     */
    public static boolean hasProvider(Class<?> clazz){
        if (associationMap.containsKey(clazz)){
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            return type instanceof AssociatedPeripheralProviderConstructor || type instanceof DualAssociator;
        }
        return false;
    }

    /**
     * Retrieves an associated consumer for the class or if no association exists return null
     * @param clazz the class of the object your checking for associations
     */
    public static<T> @Nullable AssociatedPeripheralConsumerConstructor<T> getConsumer(Class<T> clazz){
        if (associationMap.containsKey(clazz)){
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof AssociatedPeripheralConsumerConstructor<?> consumer){
                return (AssociatedPeripheralConsumerConstructor<T>) consumer;
            }
            if (type instanceof DualAssociator dualAccesser){
                return (AssociatedPeripheralConsumerConstructor<T>) dualAccesser.consumer;
            }
            return null;
        }
        return null;
    }

    /**
     * Retrieves an associated consumer for the class or if no association exists return null
     * @param clazz the class of the object your checking for associations
     */
    public static<T> @Nullable AssociatedPeripheralProviderConstructor<T> getProvider(Class<T> clazz){
        if (associationMap.containsKey(clazz)){
            PeripheralAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof AssociatedPeripheralProviderConstructor<?> provider){
                return (AssociatedPeripheralProviderConstructor<T>) provider;
            }
            if (type instanceof DualAssociator dualAccesser){
                return (AssociatedPeripheralProviderConstructor<T>) dualAccesser.provider;
            }
            return null;
        }
        return null;
    }

    /**
     * Tests if an object contains or is associated with a consumer
     * @param object the object your checking for consumers
     */
    public static <T> boolean hasConsumer(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        return hasConsumer(clazz) || object instanceof PeripheralConsumer;
    }

    /**
     * Tests if an object contains or is associated with a provider
     * @param object the object your checking for providers
     */
    public static <T> boolean hasProvider(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        return hasProvider(clazz) || object instanceof PeripheralProvider;
    }

    /**
     * Attempts to find and return a consumer that's associated with the object or implemented by the object
     * @param object the object being tested
     * @return the extracted consumer or null
     */
    public static <T> @Nullable PeripheralConsumer extractConsumer(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        if (hasConsumer(clazz)){
            AssociatedPeripheralConsumerConstructor<T> APCC = getConsumer(clazz);
            assert APCC != null;
            return APCC.onConnection(object);
        }else if (object instanceof PeripheralConsumer consumer){
            return consumer;
        }else{
            return null;
        }
    }

    /**
     * Attempts to find and return a provider that's associated with the object or implemented by the object
     * @param object the object being tested
     * @return the extracted provider or null
     */
    public static <T> @Nullable PeripheralProvider extractProvider(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        if (hasConsumer(clazz)){
            AssociatedPeripheralProviderConstructor<T> APCC = getProvider(clazz);
            assert APCC != null;
            return APCC.onConnection(object);
        }else if (object instanceof PeripheralProvider consumer){
            return consumer;
        }else{
            return null;
        }
    }
}
