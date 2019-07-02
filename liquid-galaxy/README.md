# Liquid Galaxy


## Within this repository

The "gnu_linux" directory contains example configuration files to aid
with setup of various software pieces. The philosophy employed was
one of letting the underlying distribution simplify the overall setup.

This meant leveraging the default behavior of "Xsession", the 
"alternatives" system, and various hook or "$product.d" directories.

The "php-interface" directory contains an example collection of code
used to provide a touchscreen interface for selecting queries and coordinates
to be consumed by Google Earth. Place everything into a WebServer's Docroot.

## On the system

NOTE: with SVN, it is up to the client to handle symlinks. If your client
doesn't handle symlinks, you might end up with some duplicate files with
various names, but things should still work fine.

The following tree represents the suggested directory hierarchy 
within the "lg" user's home directory:

```
/home/lg
|-- bin
|-- earth
|   |-- builds
|   |   |-- latest -> ./5.2.X.XXXX-X
|   |   `-- 5.2.X.XXXX-X
|   |-- config
|   |   |-- master
|   |   `-- slave
|   `-- scripts
|-- etc
`-- media
    `-- images
        |-- backgrounds
        `-- google
```
