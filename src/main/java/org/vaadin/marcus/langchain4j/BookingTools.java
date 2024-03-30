package org.vaadin.marcus.langchain4j;

import org.vaadin.marcus.service.BookingDetails;
import org.vaadin.marcus.service.FlightService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class BookingTools {

    private final FlightService carRentalService;

    public BookingTools(FlightService carRentalService) {
        this.carRentalService = carRentalService;
    }

    @Tool(name = "get the booking details given the booking number and the user's first and last name")
    public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName) {
        return carRentalService.getBookingDetails(bookingNumber, firstName, lastName);
    }

    @Tool(name = "book a flight given the user's first and last name, the date, and the from and to airports")
    public void changeBooking(String bookingNumber, String firstName, String lastName, String date, String from, String to) {
        carRentalService.changeBooking(bookingNumber, firstName, lastName, date, from, to);
    }

    @Tool(name = "cancel the booking given the booking number and the user's first and last name")
    public void cancelBooking(String bookingNumber, String firstName, String lastName) {
        carRentalService.cancelBooking(bookingNumber, firstName, lastName);
    }

}
