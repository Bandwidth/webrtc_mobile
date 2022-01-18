package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Person type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "People", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Person implements Model {
  public static final QueryField ID = field("Person", "id");
  public static final QueryField FIRST_NAME = field("Person", "firstName");
  public static final QueryField LAST_NAME = field("Person", "lastName");
  public static final QueryField CLIENT_ID = field("Person", "clientId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String firstName;
  private final @ModelField(targetType="String", isRequired = true) String lastName;
  private final @ModelField(targetType="String", isRequired = true) String clientId;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getFirstName() {
      return firstName;
  }
  
  public String getLastName() {
      return lastName;
  }
  
  public String getClientId() {
      return clientId;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Person(String id, String firstName, String lastName, String clientId) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.clientId = clientId;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Person person = (Person) obj;
      return ObjectsCompat.equals(getId(), person.getId()) &&
              ObjectsCompat.equals(getFirstName(), person.getFirstName()) &&
              ObjectsCompat.equals(getLastName(), person.getLastName()) &&
              ObjectsCompat.equals(getClientId(), person.getClientId()) &&
              ObjectsCompat.equals(getCreatedAt(), person.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), person.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getFirstName())
      .append(getLastName())
      .append(getClientId())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Person {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("firstName=" + String.valueOf(getFirstName()) + ", ")
      .append("lastName=" + String.valueOf(getLastName()) + ", ")
      .append("clientId=" + String.valueOf(getClientId()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static FirstNameStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Person justId(String id) {
    return new Person(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      firstName,
      lastName,
      clientId);
  }
  public interface FirstNameStep {
    LastNameStep firstName(String firstName);
  }
  

  public interface LastNameStep {
    ClientIdStep lastName(String lastName);
  }
  

  public interface ClientIdStep {
    BuildStep clientId(String clientId);
  }
  

  public interface BuildStep {
    Person build();
    BuildStep id(String id);
  }
  

  public static class Builder implements FirstNameStep, LastNameStep, ClientIdStep, BuildStep {
    private String id;
    private String firstName;
    private String lastName;
    private String clientId;
    @Override
     public Person build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Person(
          id,
          firstName,
          lastName,
          clientId);
    }
    
    @Override
     public LastNameStep firstName(String firstName) {
        Objects.requireNonNull(firstName);
        this.firstName = firstName;
        return this;
    }
    
    @Override
     public ClientIdStep lastName(String lastName) {
        Objects.requireNonNull(lastName);
        this.lastName = lastName;
        return this;
    }
    
    @Override
     public BuildStep clientId(String clientId) {
        Objects.requireNonNull(clientId);
        this.clientId = clientId;
        return this;
    }
    
    /** 
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String firstName, String lastName, String clientId) {
      super.id(id);
      super.firstName(firstName)
        .lastName(lastName)
        .clientId(clientId);
    }
    
    @Override
     public CopyOfBuilder firstName(String firstName) {
      return (CopyOfBuilder) super.firstName(firstName);
    }
    
    @Override
     public CopyOfBuilder lastName(String lastName) {
      return (CopyOfBuilder) super.lastName(lastName);
    }
    
    @Override
     public CopyOfBuilder clientId(String clientId) {
      return (CopyOfBuilder) super.clientId(clientId);
    }
  }
  
}
