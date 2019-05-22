package com.sebastianfox.food.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastianfox.food.enums.EventTypes;
import com.sebastianfox.food.enums.PrivacyTypes;
import com.sebastianfox.food.models.Event;
import com.sebastianfox.food.models.User;
import com.sebastianfox.food.repository.EventRepository;
import com.sebastianfox.food.repository.UserRepository;
import org.json.JSONException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.io.DataInput;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unused")
@Controller    // This means that this class is a Controller
@RequestMapping(path = "/api/event") // This means URL's start with /api (after Application path)
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private ObjectMapper mapper = new ObjectMapper();

    // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    @Autowired
    public EventController(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<Event> getAllUserEvents() {
        // This returns a JSON or XML with the users
        return eventRepository.findAll();
    }

    /**
     *
     * @param localeData JSON data from App
     * @return http response
     * @throws JSONException exception
     * @throws IOException exception
     */
    @RequestMapping(path = "/loadAllEvents", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> loadAllEvents(@RequestBody HashMap<String, String> localeData) throws JSONException, IOException {

        Iterable<Event> events = eventRepository.findAll();

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> hashMap = new HashMap<>();

        // Object to JSON String
        hashMap.put("events",events);
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.ACCEPTED);
    }

    /**
     *
     * @param userData JSON data from App
     * @return http response
     * @throws JSONException exception
     * @throws IOException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/loadUserEvents", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> loadUserEvents(@RequestBody HashMap<String, Object> userData) throws JSONException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> hashMap = new HashMap<>();
        User appUser = userRepository.findById((UUID) userData.get("id"));

        // check if user is available in database
        if (appUser == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        // check it getEvents is not null
        if (appUser.getEvents() == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        // attach events to response
        hashMap.put("events",appUser.getEvents());
        // Object to JSON String
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.ACCEPTED);
    }

    /**
     *
     * @param data JSON data from App
     * @return http response
     * @throws JSONException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/createNewEventByString", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> createNewEventByString(@RequestBody HashMap<String, Object> data) throws JSONException, JsonProcessingException, ParseException {

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> hashMap = new HashMap<>();

        // Initialize new event
        Event event = new Event();

        if (data.get("privacyTypes") == null || data.get("eventType") == null || data.get("user_id") == null || data.get("text") == null || data.get("date") == null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // Find owner of event by given id
        User user = userRepository.findById((UUID) data.get("user_id"));
        if (user == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        // Event type
        event.setEventType(EventTypes.valueOf(data.get("eventType").toString().toUpperCase()));

        // Event privacy
        event.setPrivacyTypes(PrivacyTypes.valueOf(data.get("privacyTypes").toString().toUpperCase()));

        // Title of event
        event.setText(data.get("text").toString());

        // Owner of event
        event.setOwner(user);

        // Date
        String dateString = data.get("date").toString();
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        event.setDate(date);

        // Optional values
        // max. Participants
        if (data.get("maxParticipants") != null) {
            event.setMaxParticipants((Integer) data.get("maxParticipants"));
        }

        // description of event
        if (data.get("description") != null) {
            event.setDescription(data.get("description").toString());
        }

        eventRepository.save(event);

        // Successful register
        hashMap.put("event", event);
        // Object to JSON String
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.CREATED);
    }

    /**
     *
     * @param data JSON data from App
     * @return http response
     * @throws JSONException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/createNewEventByObject", method = RequestMethod.POST, consumes = {"application/json"})
//    public ResponseEntity<Object> createNewEventByObject(@RequestBody Event data) throws JSONException, IOException, ParseException {
    public ResponseEntity<Object> createNewEventByObject(@RequestBody HashMap<String,Object> data) throws JSONException, IOException, ParseException {

        HashMap<String,Object> hashMap = new HashMap<>();


        Object excludedData = data.get("event");
       // Event event2 = (Event) data.get("result");
        Event event = mapper.convertValue(excludedData, Event.class);


        //String eventString = mapper.writeValueAsString(result.get("event"));
        //Event event = mapper.readValue(eventString, Event.class);
//        Event event2 = (Event) result.get("event");
        User dbUser = userRepository.findById((UUID) data.get("user"));
        event.setOwner(dbUser);;

        eventRepository.save(event);

//        userRepository.save(dbUser);

        // Successful register
        hashMap.put("event", event);
        // Object to JSON String
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.CREATED);
    }

    /**
     *
     * @param data JSON data from App
     * @return http response
     * @throws JSONException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/createOrUpdateEventByObject", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> updateEventByObject(@RequestBody HashMap<String, Object> data) throws JSONException, IOException, ParseException {

        HashMap<String,Object> hashMap = new HashMap<>();

        Object excludedData = data.get("event");

        Event event = mapper.convertValue(excludedData, Event.class);
        Event dbEvent;

        if (event.getId() != null) {
            dbEvent = eventRepository.findById(event.getId());
            dbEvent.mergeDataFromOtherInstance(event);
        } else {
            dbEvent = event;
            User dbUser = userRepository.findById((UUID) data.get("user"));
            dbEvent.setOwner(dbUser);
        }

        eventRepository.save(dbEvent);

        // Successful register
        hashMap.put("event", dbEvent);
        // Object to JSON String
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.CREATED);
    }

    /**
     *
     * @param data JSON data from App
     * @return http response
     * @throws JSONException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/attendToEvent", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> attendToEvent(@RequestBody HashMap<String, Object> data) throws JSONException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> hashMap = new HashMap<>();

        if (data.get("user_id") == null || data.get("event_id") == null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // Find event by given id
        Event event = eventRepository.findById((UUID) data.get("event_id"));
        if (event == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // Find owner of event by given id
        User user = userRepository.findById((UUID) data.get("user_id"));
        if (user == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // Check if event has available space for more members
        if (!event.hasAvailableSpaces()){
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }

        boolean isAddable = false;

        // check if privacy setting of event matches to the connection between requester and event owner
        switch(event.getPrivacyType()){
            case FRIENDS:
                isAddable = event.getOwner().getAcceptedFriends().contains(user);
                break;
            case FRIENDSOFFRIENDS:
                if (event.getOwner().getAcceptedFriends().contains(user) || event.getOwner().getFriendsOfAllFriends().contains(user)) {
                    isAddable = true;
                }
                break;
            case PRIVATE:
                // Einladungssystem fehlt noch
                isAddable = false;
                break;
            case PUBLIC:
                // blocked User Handling kann noch eingebaut werden
                isAddable = true;
                break;
            default:
                isAddable = false;
        }

        if (isAddable) {
            event.addMember(user);
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }

        // save user
        userRepository.save(user);

        // save event
        eventRepository.save(event);

        // Successful register
        hashMap.put("event", event);

        // Object to JSON String
        String jsonString = mapper.writeValueAsString(hashMap);

        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.ACCEPTED);
    }

    /**
     *
     * @param eventData JSON data from App
     * @return http response
     * @throws JSONException exception
     * @throws IOException exception
     */
    @SuppressWarnings("Duplicates")
    @RequestMapping(path = "/reloadEventById", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> reloadEventById(@RequestBody HashMap<String, Object> eventData) throws JSONException, IOException {
        // Preperation
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> hashMap = new HashMap<>();

        // Get Data
        UUID id = (UUID) eventData.get("id");
        Event event = eventRepository.findById(id);

        // Check if event is available in database
        if (event == null){
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }

        // Object to JSON String
        hashMap.put("event",event);
        String jsonString = mapper.writeValueAsString(hashMap);
        // Return to App
        return new ResponseEntity<>(jsonString, HttpStatus.ACCEPTED);
    }
}
