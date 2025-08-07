package com.redtoast.simulation.networkInterfaces;

import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;

/**
* Allows you to associate certain object types with both {@link NetworkConsumer}'s and {@link NetworkProvider}'s
 * <p>These associations are automatically used when locating an assets networking capability and can be used to give blocks and items network ability without modifying the class at all</p>
*/
public class NetworkAssociations {

    private static class DualAssociator implements NetworkAssociator<Object, Object> {
        public AssociatedNetworkingConsumerConstructor<?> consumer;
        public AssociatedNetworkingProviderConstructor<?> provider;
        public DualAssociator(AssociatedNetworkingConsumerConstructor<?> consumer, AssociatedNetworkingProviderConstructor<?> provider){
            this.consumer = consumer;
            this.provider = provider;
        }

        @Override
        public Object onConnection(Object source) {
            return null;
        }
    }

    private static final Hashtable<Class<?>, NetworkAssociator<?, ?>> associationMap = new Hashtable<>();

    /**
     * Establishes an association between a class and consumer or overwrites a pre-existing one
     * @param clazz the class of the object your associating the consumer too
     * @param consumer the consumer being associated with the class
     */
    public static <T> void createAssociation(Class<T> clazz, AssociatedNetworkingConsumerConstructor<T> consumer){
        if (consumer==null) return;
        if (!associationMap.containsKey(clazz)){
            associationMap.put(clazz, consumer);
        }else{
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof DualAssociator dualAccesser){
                dualAccesser.consumer = consumer;
                return;
            }
            if (type instanceof AssociatedNetworkingProviderConstructor<?> provider){
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
    public static <T> void createAssociation(Class<T> clazz, AssociatedNetworkingProviderConstructor<T> provider){
        if (provider==null) return;
        if (!associationMap.containsKey(clazz)){
            associationMap.put(clazz, provider);
        }else{
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof DualAssociator dualAccesser){
                dualAccesser.provider = provider;
                return;
            }
            if (type instanceof AssociatedNetworkingConsumerConstructor<?> consumer){
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
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            return type instanceof AssociatedNetworkingConsumerConstructor || type instanceof DualAssociator;
        }
        return false;
    }

    /**
     * Tests if the class has an associated provider
     * @param clazz the class of the object your checking for associations
     */
    public static boolean hasProvider(Class<?> clazz){
        if (associationMap.containsKey(clazz)){
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            return type instanceof AssociatedNetworkingProviderConstructor || type instanceof DualAssociator;
        }
        return false;
    }

    /**
     * Retrieves an associated consumer for the class or if no association exists return null
     * @param clazz the class of the object your checking for associations
     */
    public static<T> @Nullable AssociatedNetworkingConsumerConstructor<T> getConsumer(Class<T> clazz){
        if (associationMap.containsKey(clazz)){
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof AssociatedNetworkingConsumerConstructor<?> consumer){
                return (AssociatedNetworkingConsumerConstructor<T>) consumer;
            }
            if (type instanceof DualAssociator dualAccesser){
                return (AssociatedNetworkingConsumerConstructor<T>) dualAccesser.consumer;
            }
            return null;
        }
        return null;
    }

    /**
     * Retrieves an associated consumer for the class or if no association exists return null
     * @param clazz the class of the object your checking for associations
     */
    public static<T> @Nullable AssociatedNetworkingProviderConstructor<T> getProvider(Class<T> clazz){
        if (associationMap.containsKey(clazz)){
            NetworkAssociator<?, ?> type = associationMap.get(clazz);
            if (type instanceof AssociatedNetworkingProviderConstructor<?> provider){
                return (AssociatedNetworkingProviderConstructor<T>) provider;
            }
            if (type instanceof DualAssociator dualAccesser){
                return (AssociatedNetworkingProviderConstructor<T>) dualAccesser.provider;
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
        return hasConsumer(clazz) || object instanceof NetworkConsumer;
    }

    /**
     * Tests if an object contains or is associated with a provider
     * @param object the object your checking for providers
     */
    public static <T> boolean hasProvider(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        return hasProvider(clazz) || object instanceof NetworkProvider;
    }

    /**
     * Attempts to find and return a consumer that's associated with the object or implemented by the object
     * @param object the object being tested
     * @return the extracted consumer or null
     */
    public static <T> @Nullable NetworkConsumer extractConsumer(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        if (hasConsumer(clazz)){
            AssociatedNetworkingConsumerConstructor<T> APCC = getConsumer(clazz);
            assert APCC != null;
            return APCC.onConnection(object);
        }else if (object instanceof NetworkConsumer consumer){
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
    public static <T> @Nullable NetworkProvider extractProvider(T object){
        Class<T> clazz = (Class<T>) object.getClass();
        if (hasConsumer(clazz)){
            AssociatedNetworkingProviderConstructor<T> APCC = getProvider(clazz);
            assert APCC != null;
            return APCC.onConnection(object);
        }else if (object instanceof NetworkProvider consumer){
            return consumer;
        }else{
            return null;
        }
    }
}
