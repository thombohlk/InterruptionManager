//package com.example.interruptionmanager.event;
//import java.util.EventObject;
//
//
//public class WeightsEvent extends EventObject {
//
//	private static final long serialVersionUID = 1L;
// 
//    public WeightsEvent(Object source){
//        super(source);
//    }
//}
// 
//public enum MapViewEvent{
//    /**
//     * Connection to the internet has been lost.
//     */
//    CONNECTION_LOST("Connection to the internet has been lost."),
//    /**
//     * Connection to the internet has been restored.
//     */
//    CONNECTION_RESTORED("Connection to the internet has been restored"),
//    /**
//     * The LocationService has shutdown. Check logcat for errors.
//     */
//    LOCATION_EXCEPTION("There was an unknown error related to the LocationService"),
//    /**
//     * Indicates the LocationService is initialized and ready.
//     */
//    LOCATION_INITIALIZED("The LocationService has been initialized. NOTE: it still may fail after a start() attempt."),
// 
//    private String description;
//    private MapViewEvent(String description){
//        this.description = description;
//    }
//}
// 
//protected EventListenerList eventListenerList = new EventListenerList();
// 
///**
// * Adds the eventListenerList for MapViewController
// * @param listener
// */
//public void addEventListener(WeightEventListener listener){
//    eventListenerList.add(MapViewControllerEventListener.class, listener);
//}
// 
///**
// * Removes the eventListenerList for MapViewController
// * @param listener
// */
//public void removeEventListener(MapViewControllerEventListener listener){
//    eventListenerList.remove(MapViewControllerEventListener.class, listener);
//}
// 
///**
// * Dispatches CONNECTION and LOCATION events
// * @param event
// * @param message
// */
//public void dispatchEvent(MapViewControllerEvent event,String message){
//    Object[] listeners = eventListenerList.getListenerList();
//    Object eventObj = event.getSource();
//    String eventName = eventObj.toString();
//    for(int i=0; i<listeners.length;i+=2){
//        if(listeners[i] == MapViewControllerEventListener.class){
//            if(eventName.contains("CONNECTION"))
//            {
//                ((MapViewControllerEventListener) listeners[i+1]).onConnectionChangeEvent(event, message);
//            }
//            if(eventName.contains("LOCATION")){
//                ((MapViewControllerEventListener) listeners[i+1]).onLocationChangeEvent(event, message);
//            }
//        }
//    }
//}