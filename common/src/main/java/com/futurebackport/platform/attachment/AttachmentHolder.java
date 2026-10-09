package com.futurebackport.platform.attachment;

import java.util.Map;

/** Implemented on every entity by {@code EntityAttachmentMixin}: the attachment values set on that entity. */
public interface AttachmentHolder {

    Map<String, Object> futurebackport$attachments();
}
