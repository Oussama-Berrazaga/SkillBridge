# 📊 Data Models

### Listing Service Entities

```mermaid
classDiagram
    class Category {
        +Long id
        +String name
        +Category parent
        +List~Category~ subCategories
    }

    class Listing {
        +Long id
        +String title
        +String description
        +Status status
        +List~Category~ categories
    }

    class Application {
        +Long id
        +Long technicianId
        +String message
        +ApplicationStatus status
    }

    Category "0..1" --> "*" Category : parent
    Listing "1" *-- "*" Application : contains
    Listing "*" --o "*" Category : categorized_by


```
