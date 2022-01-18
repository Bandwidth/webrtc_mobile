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

/** This is an auto generated class representing the DeviceInfo type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "DeviceInfos", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class DeviceInfo implements Model {
  public static final QueryField ID = field("DeviceInfo", "id");
  public static final QueryField NOTIFY_TYPE = field("DeviceInfo", "notifyType");
  public static final QueryField DEVICE_TOKEN = field("DeviceInfo", "deviceToken");
  public static final QueryField PARTICIPANT_ID = field("DeviceInfo", "participantId");
  public static final QueryField PARTICIPANT_TOKEN = field("DeviceInfo", "participantToken");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String notifyType;
  private final @ModelField(targetType="String", isRequired = true) String deviceToken;
  private final @ModelField(targetType="String") String participantId;
  private final @ModelField(targetType="String") String participantToken;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getNotifyType() {
      return notifyType;
  }
  
  public String getDeviceToken() {
      return deviceToken;
  }
  
  public String getParticipantId() {
      return participantId;
  }
  
  public String getParticipantToken() {
      return participantToken;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private DeviceInfo(String id, String notifyType, String deviceToken, String participantId, String participantToken) {
    this.id = id;
    this.notifyType = notifyType;
    this.deviceToken = deviceToken;
    this.participantId = participantId;
    this.participantToken = participantToken;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      DeviceInfo deviceInfo = (DeviceInfo) obj;
      return ObjectsCompat.equals(getId(), deviceInfo.getId()) &&
              ObjectsCompat.equals(getNotifyType(), deviceInfo.getNotifyType()) &&
              ObjectsCompat.equals(getDeviceToken(), deviceInfo.getDeviceToken()) &&
              ObjectsCompat.equals(getParticipantId(), deviceInfo.getParticipantId()) &&
              ObjectsCompat.equals(getParticipantToken(), deviceInfo.getParticipantToken()) &&
              ObjectsCompat.equals(getCreatedAt(), deviceInfo.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), deviceInfo.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getNotifyType())
      .append(getDeviceToken())
      .append(getParticipantId())
      .append(getParticipantToken())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("DeviceInfo {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("notifyType=" + String.valueOf(getNotifyType()) + ", ")
      .append("deviceToken=" + String.valueOf(getDeviceToken()) + ", ")
      .append("participantId=" + String.valueOf(getParticipantId()) + ", ")
      .append("participantToken=" + String.valueOf(getParticipantToken()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static NotifyTypeStep builder() {
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
  public static DeviceInfo justId(String id) {
    return new DeviceInfo(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      notifyType,
      deviceToken,
      participantId,
      participantToken);
  }
  public interface NotifyTypeStep {
    DeviceTokenStep notifyType(String notifyType);
  }
  

  public interface DeviceTokenStep {
    BuildStep deviceToken(String deviceToken);
  }
  

  public interface BuildStep {
    DeviceInfo build();
    BuildStep id(String id);
    BuildStep participantId(String participantId);
    BuildStep participantToken(String participantToken);
  }
  

  public static class Builder implements NotifyTypeStep, DeviceTokenStep, BuildStep {
    private String id;
    private String notifyType;
    private String deviceToken;
    private String participantId;
    private String participantToken;
    @Override
     public DeviceInfo build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new DeviceInfo(
          id,
          notifyType,
          deviceToken,
          participantId,
          participantToken);
    }
    
    @Override
     public DeviceTokenStep notifyType(String notifyType) {
        Objects.requireNonNull(notifyType);
        this.notifyType = notifyType;
        return this;
    }
    
    @Override
     public BuildStep deviceToken(String deviceToken) {
        Objects.requireNonNull(deviceToken);
        this.deviceToken = deviceToken;
        return this;
    }
    
    @Override
     public BuildStep participantId(String participantId) {
        this.participantId = participantId;
        return this;
    }
    
    @Override
     public BuildStep participantToken(String participantToken) {
        this.participantToken = participantToken;
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
    private CopyOfBuilder(String id, String notifyType, String deviceToken, String participantId, String participantToken) {
      super.id(id);
      super.notifyType(notifyType)
        .deviceToken(deviceToken)
        .participantId(participantId)
        .participantToken(participantToken);
    }
    
    @Override
     public CopyOfBuilder notifyType(String notifyType) {
      return (CopyOfBuilder) super.notifyType(notifyType);
    }
    
    @Override
     public CopyOfBuilder deviceToken(String deviceToken) {
      return (CopyOfBuilder) super.deviceToken(deviceToken);
    }
    
    @Override
     public CopyOfBuilder participantId(String participantId) {
      return (CopyOfBuilder) super.participantId(participantId);
    }
    
    @Override
     public CopyOfBuilder participantToken(String participantToken) {
      return (CopyOfBuilder) super.participantToken(participantToken);
    }
  }
  
}
