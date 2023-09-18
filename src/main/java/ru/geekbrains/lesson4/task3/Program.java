package ru.geekbrains.lesson4.task3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Program {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");

/**
 * Разработать контракты и компоненты системы "Покупка онлайн билетов на автобус в час пик".
 *
 * 1.  Предусловия.
 * 2.  Постусловия.
 * 3.  Инвариант.
 * 4.  Определить абстрактные и конкретные классы.
 * 5.  Определить интерфейсы.
 * 6.  Реализовать наследование.
 * 7.  Выявить компоненты.
 * 8.  Разработать Диаграмму компонент использую нотацию UML 2.0. Общая без деталей.
 */
    public static void main(String[] args) {

        Core core = new Core();
        MobileApp mobileApp = new MobileApp(core.getTicketProvider(), core.getCustomerProvider());
        BusStation busStation = new BusStation(core.getTicketProvider());
        if (mobileApp.buyTicket("1111111111111111")){
            System.out.println("Клиент успешно купил билет.");
            Date date = new Date();
            date.setTime(1694998359337L);
            mobileApp.searchTicket(date);
            Collection<Ticket> tickets = mobileApp.getTickets();
            if (busStation.checkTicket(tickets.stream().findFirst().get().getQrcode())){
                System.out.println("Клиент успешно прошел в автобус.");
            }
        }


    }

}

class Core{

    private final CustomerProvider customerProvider;
    private final TicketProvider ticketProvider;
    private final PaymentProvider paymentProvider;
    private final Database database;

    public Core(){
        database = new Database();
        customerProvider = new CustomerProvider(database);
        paymentProvider = new PaymentProvider();
        ticketProvider = new TicketProvider(database, paymentProvider);
    }

    public TicketProvider getTicketProvider() {
        return ticketProvider;
    }

    public CustomerProvider getCustomerProvider() {
        return customerProvider;
    }

}


/**
 * Покупатель
 */
class Customer{

    private static int counter;

    private final int id;

    private Collection<Ticket> tickets = new ArrayList<>();

    {
        id = ++counter;
    }

    public Collection<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Collection<Ticket> tickets) {
        this.tickets = tickets;
    }

    public int getId() {
        return id;
    }

}

/**
 * Билет
 */
class Ticket{

    private int id;

    private int customerId;

    private Date date;

    private String qrcode;

    private boolean enable = true;

    public Ticket() {
    }

    public Ticket(int id, int customerId, Date date, String qrcode) {
        this.id = id;
        this.customerId = customerId;
        this.date = date;
        this.qrcode = qrcode;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Date getDate() {
        return date;
    }

    public String getQrcode() {
        return qrcode;
    }

    public boolean isEnable() {
        return enable;
    }
}


/**
 * База данных
 */
class Database{

    private static int counter;
    private Collection<Ticket> tickets = new ArrayList<>();
    private Collection<Customer> customers = new ArrayList<>();

    public Database() {
        Date date = new Date();
        date.setTime(1694998359337L);
        for (int i = 0; i < 10; i++) {
            tickets.add(new Ticket(
                    i,
                    i,
                    date,
                    "qrcode"
            ));
        }
    }

    public Collection<Ticket> getTickets() {

        return tickets;
    }

    public Collection<Customer> getCustomers() {
        return customers;
    }

    /**
     * Получить актуальную стоимость билета
     * @return
     */
    public double getTicketAmount(){
        return 45;
    }

    /**
     * Получить идентификатор заявки на покупку билета
     * @return
     */
    public int createTicketOrder(int clientId){
        return ++counter;
    }

}

class PaymentProvider{

    public boolean buyTicket(int orderId, String cardNo, double amount){
        //TODO: Обращение к платежному шлюзу, попытка выполнить списание средств ...
        return true;
    }

}

/**
 * Мобильное приложение
 */
class MobileApp{

    private final Customer customer;
    private final TicketProvider ticketProvider;
    private final CustomerProvider customerProvider;


    public MobileApp(TicketProvider ticketProvider, CustomerProvider customerProvider) {
        this.ticketProvider = ticketProvider;
        this.customerProvider = customerProvider;
        customer = customerProvider.getCustomer("<login>", "<password>");

    }

    public Collection<Ticket> getTickets(){
        return customer.getTickets();
    }

    public void searchTicket(Date date){
        date = new Date();
        date.setTime(1694998359337L);
        customer.setTickets(ticketProvider.searchTicket(customer.getId(), date));
    }

    public boolean buyTicket(String cardNo){
        return ticketProvider.buyTicket(customer.getId(), cardNo);
    }

}

class TicketProvider{

    private final Database database;
    private final PaymentProvider paymentProvider;

    public TicketProvider(Database database, PaymentProvider paymentProvider){
        this.database = database;
        this.paymentProvider = paymentProvider;
    }

    public Collection<Ticket> searchTicket(int clientId, Date date){

        // Предусловие
        if (clientId <= 0) {
            throw new RuntimeException("Идентификатор клиента не может быть равен или меньше нуля!");
        }
        if (date == null) {
            throw new RuntimeException("Дата для поиска билета не может быть пустой (null)");
        }

        Collection<Ticket> tickets = new ArrayList<>();
        // Инвариант
        findTicketsByClientIdAndDate(clientId, date, tickets);

        // Постусловие
        // Считаю, что возвращать пустой список в данной ситуации будет нормой.

        return tickets;

    }

    public boolean buyTicket(int clientId, String cardNo){

        // Предусловие
        if (clientId <= 0) {
            throw new RuntimeException("Идентификатор клиента не может быть равен или меньше нуля!");
        }
        if (cardNo == null || (cardNo.length() != 16)) {
            throw new RuntimeException("Номер карты должен состоять из 16 цифр");
        }

        int orderId = database.createTicketOrder(clientId);
        double amount = database.getTicketAmount();

        boolean isBought = paymentProvider.buyTicket(orderId, cardNo, amount);

        if (!isBought) {
            throw new RuntimeException("Произошла ошибка при покупке билета. " +
                    "Повторите снова или обратитесь в техническую поддержку.");
        }

        return isBought;
    }

    public boolean checkTicket(String qrcode){
        for (Ticket ticket: database.getTickets()) {
            if (ticket.getQrcode().equals(qrcode)){
                ticket.setEnable(false);
                // Save database ...
                return true;
            }
        }
        return false;
    }

    private void findTicketsByClientIdAndDate(int clientId, Date date, Collection<Ticket> tickets) {
        for (Ticket ticket: database.getTickets()) {
            if (ticket.getCustomerId() == clientId && ticket.getDate().equals(date))
                tickets.add(ticket);
        }
    }

}

class CustomerProvider{

    private Database database;

    public CustomerProvider(Database database) {
        this.database = database;
    }

    public Customer getCustomer(String login, String password){
        return new Customer();
    }

}

/**
 * Автобусная станция
 */
class BusStation{

    private final TicketProvider ticketProvider;

    public BusStation(TicketProvider ticketProvider){
        this.ticketProvider = ticketProvider;
    }

    public boolean checkTicket(String qrCode){
        return ticketProvider.checkTicket(qrCode);
    }

}


