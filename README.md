## NEWORK
## ВНЕРАБОТЫ

Данный проект разработан в качестве дипломной работы годового курса "Android-разработчик с нуля" в университете «Нетология» (ООО «Нетология»). 

Проект представляет из себя мобильное приложение для социальной сети, в котором пользователи могут созадавать и редактировать посты с медиа(изображения, видео и аудио) и указанием локации. 
Одновременно с этим имееться возможность создавать посты типа "События" с указанием места и времени проведения события. 
В своем профиле пользователи могут указывать места своей работы.

### Инструменты и технологии используемые в проекте.
- Архитектура MVVM
- Библиотеки:
    - Material Design
    - ROOM
    - OKHTTP
    - Retrofit
    - Hilt
    - LiveData, Flow
    - Coroutines
    - YandexMapsMobile
    - ImagePicker, Glide
    - [Avatarview](https://github.com/GetStream/avatarview-android) - работа с аватарками

## Основы интерфейса
Интерфейс разработан на основе дизайна разработанного на в [Figma](https://www.figma.com/file/8z1sV6KIf6Sc1y02TrY2XS/Nmedia?type=design&node-id=0%3A1&mode=design&t=P0JIwE4Xj28DOx61-1)

### Основной экран приложения.
Основой экран приложения содержит нижнее меню с кнопками переключения между экранами "Посты", "События", "Пользователи" и верхнее меню с кнопкой входа(регистрации) в приложение или, если пользователь авторизован, 
кнопку перехода на экран просмтора профиля пользователя.

![Posts](https://github.com/BAn66/NeWork/blob/master/sceenshots/mainPosts.JPG?raw=true) 
![Events](https://github.com/BAn66/NeWork/blob/master/sceenshots/events.JPG?raw=true)  
![Users](https://github.com/BAn66/NeWork/blob/master/sceenshots/users.JPG?raw=true)

Приложением предусмотрена возможность проставления реакции "лайка" для понравившихся постов, а также имеется фунуция отслеживания их количества.
Пользованель может поделится текстом поста по электронной почте или иным способом.

### При нажатии на пост/событие/пользователя осуществляется переход на расширенную карточку поста/события/пользователя с его подробным описанием

![PostDetails](https://github.com/BAn66/NeWork/blob/master/sceenshots/postDetails.JPG?raw=true) 
![EventDetails](https://github.com/BAn66/NeWork/blob/master/sceenshots/eventDetails.JPG?raw=true)
![UserDetails](https://github.com/BAn66/NeWork/blob/master/sceenshots/userDetails.JPG?raw=true)

### При создании поста можно выбирать медиа контент, упомянутых пользователей, точку геолокации

![NewPost](https://github.com/BAn66/NeWork/blob/master/sceenshots/newPost.JPG?raw=true)
![TakePeople](https://github.com/BAn66/NeWork/blob/master/sceenshots/takePeople.JPG?raw=true)
![TakeLocation](https://github.com/BAn66/NeWork/blob/master/sceenshots/takePlace.JPG?raw=true)

### При создании события можно добавлять медиа контент, выбирать пользователей-спикеров на мероприятии, дату, время и тип события (онлайн или оффлайн), а также место его проведения.

![NewEvent](https://github.com/BAn66/NeWork/blob/master/sceenshots/newEvent.JPG?raw=true)
![SetDateTimeEvent](https://github.com/BAn66/NeWork/blob/master/sceenshots/takeDateTimeTypeEvent.JPG?raw=true)

### Экраны входа и регистрации пользователя
При регистрации пользователь имеет возможность указать имя которое будет отображаться у других, и выбрать себе аватарку

![Login](https://github.com/BAn66/NeWork/blob/master/sceenshots/login.JPG?raw=true)
![Registration](https://github.com/BAn66/NeWork/blob/master/sceenshots/registration.JPG?raw=true)

### На эркране деталей о пользователе есть вкладки с его постами и местами работы. 
Если пользователь авторизован, то он может добавлять или удалять посты и места своей работы.

![UsersJobs](https://github.com/BAn66/NeWork/blob/master/sceenshots/userDetailsJobs.JPG?raw=true)
![NewJob](https://github.com/BAn66/NeWork/blob/master/sceenshots/NewJob.JPG?raw=true)

### Реализована локализация на русский и английский языки.

## Разработано Костенко Константином.
telegramm: [@NMAdminAstra](https://t.me/NMAdminAstra)
github: [BAn66](https://github.com/BAn66)
e-mail: thetanqel@gmail.com