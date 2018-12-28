(function () {
    var i18nData = {
        en: {
            About_Code: 'Code',
            About_Graphics: 'Graphics',
            About_MusicAndSound: 'Music and sound',
            About_Music: 'Music',
            About_Sounds: 'Sounds',
            About_Levels: 'Levels',
            About_Licenses: 'Licenses',
            Licenses_Back: 'Back',
            Error_Title: 'This webpage is not available',
            Error_Code: 'Error code',
            Error_Description: 'Error description'
        },
        ru: {
            About_Code: 'Код',
            About_Graphics: 'Графика',
            About_MusicAndSound: 'Музыка и звуки',
            About_Music: 'Музыка',
            About_Sounds: 'Звуки',
            About_Levels: 'Уровни',
            About_Licenses: 'Лицензии',
            Licenses_Back: 'Назад',
            Error_Title: 'Запрашиваемая страница недоступна',
            Error_Code: 'Код ошибки',
            Error_Description: 'Описание ошибки'
        }
    };

    function parseParams() {
        var parts = location.search.replace(/^\?/, '').split('&');
        var params = {};

        for (var i = 0, len = parts.length; i < len; i++) {
            var pair = parts[i].split(/=/);
            params[decodeURIComponent(pair[0])] = decodeURIComponent((pair[1] || '').replace(/\+/g, ' '));
        }

        return params;
    }

    function replaceLinks() {
        var elements = document.querySelectorAll('[data-link]');
        var search = location.search.replace(/^\?/, '');

        for (var i = 0, len = elements.length; i < len; i++) {
            var element = elements[i];
            var link = 'file:///android_asset/web/' + element.getAttribute('data-link');

            if (search !== '') {
                link += '?' + search;
            }

            element.setAttribute('href', link);
            element.href = link;
        }
    }

    function replaceStrings(params) {
        var elements = document.querySelectorAll('[data-string]');

        for (var i = 0, len = elements.length; i < len; i++) {
            var element = elements[i];
            var key = element.getAttribute('data-string');
            var append = element.getAttribute('data-string-append');
            var text = params[key] || key;

            if (append !== void 0 && append !== null && append !== '') {
                text += append;
            }

            element.innerText = text;
        }
    }

    var params = parseParams();
    var strings = i18nData[params.language] || i18nData.en;

    strings.Param_Language = params.language;
    strings.Param_AppName = params.appName || '';
    strings.Param_AppVersion = params.appVersion || '';

    setTimeout(function () {
        replaceLinks();
        replaceStrings(strings);
    }, 0);
})();
