package com.github.ixtf.cqrs.annotations

import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.workflow.Workflow
import kotlin.reflect.KClass

annotation class Consume {
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class FromKeyValueEntity(
    /**
     * Assign the class type of the entity one intends to consume from, which must extend
     * [com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity].
     */
    val value: KClass<out KeyValueEntity<*>>
  )

  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class FromEventSourcedEntity(
    /**
     * Assign the class type of the entity one intends to consume from, which must extend
     * [com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity].
     */
    val value: KClass<out EventSourcedEntity<*, *>>,

    /**
     * This option is only available for classes. Using it in a method has no effect.
     *
     * When there is no method in the class whose input type matches the event type:
     * * if ignoreUnknown is true the event is discarded
     * * if false, an Exception is raised
     */
    val ignoreUnknown: Boolean = false,
  )

  /**
   * Annotation for consuming state updates from a [com.github.ixtf.cqrs.workflow.Workflow].
   *
   * The underlying method must be declared to receive one parameter for the received workflow state
   * changes.
   */
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class FromWorkflow(
    /**
     * Assign the class type of the workflow one intends to consume from, which must extend
     * [com.github.ixtf.cqrs.workflow.Workflow].
     */
    val value: KClass<out Workflow<*>>
  )

  /**
   * Annotation for consuming messages from a topic (i.e PubSub or Kafka topic).
   *
   * The underlying method must be declared to receive one parameter for the received messages. Use
   * one method with the common message type as parameter, or several methods with different
   * parameter types corresponding to different messages types.
   */
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class FromTopic(
    /** Assign the name of the topic to consume the stream from. */
    val value: String,
    /** Assign the consumer group name to be used on the broker. */
    val consumerGroup: String = "",
    /**
     * This option is only available for classes. Using it in a method has no effect.
     *
     * When there is no method in the class whose input type matches the event type:
     * * if ignoreUnknown is true the event is discarded
     * * if false, an Exception is raised
     */
    val ignoreUnknown: Boolean = false,
  )

  /**
   * Annotation for consuming messages from another service.
   *
   * The underlying method must be declared to receive one parameter for the received messages. Use
   * one method with the common event type as parameter, or several methods with different parameter
   * types corresponding to different message types.
   */
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class FromServiceStream(
    /** The unique identifier of the stream in the producing service */
    val id: String,
    /**
     * The deployed name of the service to consume from, can be the deployed name of another service
     * in the same project or a fully qualified public hostname of a service in a different project.
     *
     * Note: The service name is used as unique identifier for tracking progress when consuming it.
     * Changing this name will lead to starting over from the beginning of the event stream.
     *
     * Can be a template referencing an environment variable "${MY_ENV_NAME}" set for the service at
     * deployment.
     */
    val service: String,
    /**
     * In case you need to consume the same stream multiple times, each subscription should have a
     * unique consumer group.
     *
     * Changing the consumer group will lead to starting over from the beginning of the stream.
     */
    val consumerGroup: String = "",
    /**
     * When there is no method in the class whose input type matches the event type:
     * * if ignoreUnknown is true the event is discarded
     * * if false, an Exception is raised
     */
    val ignoreUnknown: Boolean = false,
  )
}
