# jakarta-package-alignment

foo

jakarta-package-alignment is a gradle plugin that tries to enforce that projects
which depend on a library from Jakarta EE use a version known to have
updated namespaces to `jakarta`. If projects have a dependency on a Jakarta
EE library that still includes the old `javax` namespace, it will forcibly
replace that dependency with the corresponding `javax` library from the
Java EE spec.

It does this by examining the version of known Jakarta EE dependencies. If the
requested version is less than the version from the Jakarta EE 9 spec (which,
presumably, moves all namespaces from `javax` to `jakarta`), then this plugin
will forcibly replace the dependency with a corresponding one from the Java EE
8 spec with the highest version.

As an example, if a project has a dependency on

    jakarta.servlet:jakarta.servlet-api:4.0.4

which is known to still use the old `javax` namespace instead of `jakarta`, it
will be replaced with

    javax.servlet:javax.servlet-api:4.0.1

instead (version `4.0.1` is the highest version of `javax.servlet-api` from
the Java EE 8 spec at the time of this writing).

On the other hand, if a project has a dependency on `jakarta.servlet-api:5.0.0`
(from the Jakarta EE 9 spec), this plugin will do nothing.

## Rationale

The [javax -> jakarta rename](https://waynebeaton.wordpress.com/2019/04/04/renaming-java-ee-specifications-for-jakarta-ee/)
moved classes that were previously part of the Java EE spec (now the Jakarta 
EE spec) from namespace `javax` to namespace `jakarta`. Unfortunately,
Jakarta EE 8 is fully compatible with Java EE 8, meaning that there are some
versions of jars from the Jakarta EE implementation with classes still
residing under the `javax` namespace. To avoid any confusion, this plugin
tries to enforce a clear delineation between usage of classes in `javax`
and `jakarta` namespaces by requiring that any usage of class versions under
the `javax` namespace come from the Java EE libraries, and not the Jakarta
libraries.

## Implementation

This plugin contains a preset list of Jakarta EE library names and known
"bad" versions (defined as the maximum Jakarta EE version that still uses
the `javax` namespace). It also contains a preset list of mappings from
those Jakarta EE libraries to the corresponding Java EE library version
(if one exists).

The plugin uses [gradle dependency substitution](https://docs.gradle.org/current/userguide/resolution_rules.html#sub:conditional_dependency_substitution)
to check if a project requests a Jakarta EE dependency with a version
less than or equal to the maximum "bad" version; if it encounters one,
it replaces it with the mapped Java EE library version instead.

## Caveats with [gradle-consistent-versions](https://github.com/palantir/gradle-consistent-versions)

This plugin works with the gradle-consistent-versions plugin, but
currently only correctly replaces transitive Jakarta dependencies
discovered on the dependency graph.

If you project declares, e.g. a direct dependency such as this:

    # in versions.props
    jakarta.servlet:jakarta.servlet-api = 4.0.4
    ...
    # in build.gradle
    implementation 'jakarta.servlet:jakarta.servlet-api'

then you may see a failure like this one:

    > Could not compute lock state from configuration 'unifiedClasspath' due to unresolved dependencies:
       * jakarta.servlet:jakarta.servlet-api (requested: 'jakarta.servlet:jakarta.servlet-api' because: requested)
            Failures:
               - Could not find jakarta.servlet:jakarta.servlet-api:.

In this case, you should update your project to either: (a) declare
an explicit dependency on the corresponding Java EE library instead
(e.g. use `javax.servlet:javax.servlet-api`); or (b) use a newer
version of the Jakarta EE dependency with the updated `jakarta`
namespace.

## License

This project is made available under the
[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

