package cloudos.appstore.model;

import lombok.experimental.Accessors;

import javax.persistence.Entity;

@Entity @Accessors(chain=true)
public class CloudAppVersion extends AppVersionBase {}
