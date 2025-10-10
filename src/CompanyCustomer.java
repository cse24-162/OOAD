public class CompanyCustomer extends Customer {
    public String companyName;
    private long registrationNumber;
    public String contactPerson;

   public CompanyCustomer(String companyName, long registrationNumber, String contactPerson,
                          String name,long contactNumber, String address) {
       super(name,contactNumber, address);
       this.companyName = companyName;
       this.registrationNumber = registrationNumber;
       this.contactPerson = contactPerson;
   }

   public String getCompanyName() {
       return companyName;
   }
   public void setCompanyName(String companyName) {
       this.companyName = companyName;
   }
   public long getRegistrationNumber() {
       return registrationNumber;
   }
   public void setRegistrationNumber(long registrationNumber) {
       this.registrationNumber = registrationNumber;
   }
   public String getContactPerson() {
       return contactPerson;
   }
   public void setContactPerson(String contactPerson) {
       this.contactPerson = contactPerson;
   }

}
