import java.util.Date;

public class IndividualCustomer extends Customer{
    private Date dateOfBirth;
    public String employerName;
    public String employerAddress;

    public IndividualCustomer(String name, long contactNumber, String address,
                              Date dateOfBirth, String employerName, String employerAddress) {
        super(name, contactNumber, address);
        this.dateOfBirth = dateOfBirth;
        this.employerName = employerName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getEmployerName() {
        return employerName;
    }
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }
    public String getEmployerAddress() {
        return employerAddress;
    }
    public void setEmployerAddress(String employerAddress) {
        this.employerAddress = employerAddress;
    }

}
