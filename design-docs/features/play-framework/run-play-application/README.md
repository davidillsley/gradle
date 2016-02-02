# Developer runs a Play application

- [x] Developer runs a basic Play application
- [x] Play application stays running across continuous builds
- [x] Play application is reloaded when changes are made to local source
- [x] Multi-project Play application is reloaded when changes are made to local source
- [x] Scala compile process is reused across continuous build instances

## Backlog & Open Issues

- Debugging Play application that is run with playRun
  - currently there isn't a way to add debugging JVM arguments to the worker process
- PlayRun uses org.gradle.api.tasks.compile.BaseForkOptions for forkOptions. That class is documented to hold forked compiler JVM arguments.
- Story: Build of pending changes is deferred until reload is requested by Play application
- Story: Resources are built on demand when running Play application
- Story: Long running compiler daemons

### Open Issues

- Need to figure out what to do with deployment handles when various project/model changes take place in between builds:
  - Binary name changes (currently not possible)
  - Platform changes (i.e. need to stop any deployment handles from other platforms before starting the new one)
  - Project name changes
  - PlayRun task config changes (e.g. fork options)

## Stories

### Story: Developer runs a basic Play application

Extend the Play support to allow the Play application to be executed.

- Running `gradle assemble` produces an executable Jar that, when executed, runs the Play application.
- Running `gradle run<ComponentName>` builds and executes the Play application.

At this stage, only the default generated Play application is supported, with a hard-coded version of Scala and Play.

#### Test cases

- verify that play app can be built and executed with play version 2.3.7 and 2.2.3
- Can configure port for launched PlayApp: default is 9000

### Story: Play application stays running across continuous builds

This story builds the mechanics necessary to have the play run task launch the Play process without blocking the build.
This allows continuous build to function as normal.
Actually reloading the Play application is out of scope for this story.
The application can still respond to changes to assets that are generated by the build (e.g. processed images).

No public API or functionality is required.
All infrastructure can be internal and be just enough to meet the requirements for running Play.

#### Implementation

- Introduce `BuildSessionScopeServices` which is a PluginServiceRegistry scope that fits between GlobalScopeServices and BuildScopeServices.
It manages the lifecycle of services that should exist across multiple builds in a continuous build session, but should not extend across
multiple continuous build sessions (in a long-living process like the daemon).

- Introduce a Deployment Registry


    // Gradle service, that outlives a single build (i.e. maybe global)
    // Accessible to tasks via service extraction
    @ThreadSafe
    interface DeploymentRegistry extends Stoppable {
      void register(DeploymentHandle handle);
      <T extends DeploymentHandle> void get(Class<T> handleType, String id);
    }

    interface DeploymentHandle extends Stoppable {
      String getId();
      boolean isRunning();
    }

When continuously building, the play run task, on first invocation, will register a deployment handle for the play process.
Subsequent invocations will do nothing.

#### Test coverage

- ~~Play application stays running across continuous build instances (implicitly: > 1 instances are never launched)~~
- ~~Build failure prior to launching play app (e.g. compile failure) does not prevent app from being launched on subsequent build~~
- ~~Play application is shutdown when build is cancelled~~
- ~~PlayRun task blocks when not continuously building~~
- ~~Can be used from Tooling API, deployment is stopped when build is cancelled~~
- ~~Two projects in multiproject build can start deployments~~

### Story: Play application is reloaded when changes are made to local source

This story adds integration with Play's mechanics to reloading the application implementation.
No changes to continuous build are required.
Builds are triggered as soon as changes are detected.

#### Implementation

On reload, the play run task retrieves the deployment handle (i.e. by Play specific subtype) and “triggers a reload”.
The worker protocol needs to be slightly expanded to allow communication of the new classpath/reload request.
Existing BuildLink adapter can be used with minor modifications.

#### Test coverage

- ~~User changes a controller and sees the change reflected in the running app.~~
- ~~User changes a route and sees the change reflected in the running app.~~
- ~~User changes a model class and sees the change reflected in the running app.~~
- ~~User changes a twirl template and sees the change reflected in the running app.~~
- ~~User changes a static asset and sees the change reflected in the running app.~~
- ~~Reload is not triggered if dependency of play run task fails~~

### Story: Multi-project Play application is reloaded when changes are made to local source

#### Implementation

The Play BuildLink solution returns a new classloader that contains the "changing" application resources. The non-changing application resources are part of the parent classloader for the classloader returned by the BuildLink implementation.
In a multi-project Play application, the "changing" application resources are considered to be the application jar of the current project and all dependencies that are of a Project component type. All other resources in the runtime classpath are part of the "non-changing" resources and handled by the parent classloader.
 - dependencies of project component type will be filtered by checking `ResolvedArtifact.getId().getComponentIdentifier() instanceof ProjectComponentIdentifier`

#### Test coverage

- Changes to source are reflected in running play app
  - test changes to primary app and submodules

### Story: Scala compile process is reused across continuous build instances

This is a performance optimization to the implementation of Play reload, that decreases compile times by reusing compiler daemons.

#### Implementation

- Move `CompilerDaemonManager` to session scope.  This will pull along:
  - WorkerProcessFactory - Only implication is that worker process id won't be reset on every build (i.e. it will continue to increment over subsequent builds).
  - CacheRepository - No implications.
  - ClassPathRegistry - This doesn't actually get pulled along, we just end up with a new classpath registry at build session scope containing WorkerProcessClassPathProvider.
  - WorkerProcessClasspathProvider - No implications.
- This will affect compilers for:
  - Java
  - Scala
  - Groovy
  - Play Routes
  - Play Twirl
  - Play Javascript

#### Test coverage

- Verify that the following compilers are reused across continuous build invocations:
  - ZincScalaCompiler
  - JdkJavaCompiler
  - ApiGroovyCompiler
  - RoutesCompiler
  - TwirlCompiler
  - GoogleClosureCompiler
- Verify that WorkerProcessFactory is reused across continuous builds.
- Verify that CacheRepository is reused across continuous builds.
- Verify that ClassPathRegistry is reused across continuous builds.
- Verify that WorkerProcessClasspathProvider is reused across continuous builds.

## Candidate Stories

### Story: Build of pending changes is deferred until reload is requested by Play application

This story aligns the behavior of the Play reload support closer to SBT's implementation.
Instead of building as soon as local changes are noticed, building will be deferred until the next request to the Play application.
This improves usability by not doing the work of _applying_ changes until the developer is ready to test them.

*TBD - value/priority of feature is not yet understood*.

### Story: Resources are built on demand when running Play application

When running a Play application, start the application without building any resources. Build these resources only when requested
by the client.

- On each request, check whether the task which produces the requested resource has been executed or not. If not, run the task synchronously
  and block until completed.
- Include the transitive input of these tasks as inputs to the watch mechanism, so that further changes in these source files will
  trigger a restart of the application at the appropriate time.
- Failures need to be forwarded to the application for display.

## Story: Long running compiler daemons

Reuse the compiler daemon across builds to keep the Scala compiler warmed up. This is also useful for the other compilers.

### Implementation

- Maintain a registry of compiler daemons in ~/.gradle
- Daemons expire some time after build, with much shorter expiry than the build daemon.
- Reuse infrastructure from build daemon.


