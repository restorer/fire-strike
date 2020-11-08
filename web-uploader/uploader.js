var lib = (function() {
    var AJAX_MAX_TRIES = 8;

    var loaderMask = 0;
    var ajaxIsActive = false;
    var ajaxQueue = [];
    var getXMLHttpCode = null;

    function getXMLHttp() {
        var req = null;

        if (getXMLHttpCode !== null) {
            eval(getXMLHttpCode);
            return req;
        }

        if (window.XMLHttpRequest) {
            getXMLHttpCode = 'req=new XMLHttpRequest()';
            eval(getXMLHttpCode);
        } else if (window.ActiveXObject) {
            var msxmls = ['Msxml2.XMLHTTP.5.0', 'Msxml2.XMLHTTP.4.0', 'Msxml2.XMLHTTP.3.0', 'Msxml2.XMLHTTP', 'Microsoft.XMLHTTP'];

            for (var i = 0; i < msxmls.length; i++) {
                try {
                    getXMLHttpCode = "req=new ActiveXObject('" + msxmls[i] + "')";
                    eval(getXMLHttpCode);
                    break;
                } catch (ex) {
                    getXMLHttpCode = null;
                }
            }
        }

        if (req === null || req === false) {
            return null;
        }

        return req;
    }

    return {
        LOADER_MASK_AJAX: 1,

        quoteRegExp: function(str) {
            return (str + '').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
        },

        htmlEscape: function(str) {
            return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        },

        each: function(list, callback) {
            for (var idx = 0; idx < list.length; idx++) {
                callback(list[idx], idx);
            }
        },

        eachKey: function(object, callback) {
            for (var key in object) {
                if (object.hasOwnProperty(key)) {
                    callback(key, object[key]);
                }
            }
        },

        query: function(selectorOrElementOrList) {
            var elements;

            if (typeof(selectorOrElementOrList) === 'string') {
                elements = document.querySelectorAll(selectorOrElementOrList);
            } else if (selectorOrElementOrList !== null
                && typeof(selectorOrElementOrList) === 'object'
                && typeof(selectorOrElementOrList.length) === 'number'
                && Object.prototype.toString.call(selectorOrElementOrList) === '[object Array]'
            ) {
                elements = selectorOrElementOrList;
            } else {
                elements = [selectorOrElementOrList];
            }

            function makeProperties(name, value) {
                if (typeof(name) == 'object') {
                    return name;
                } else {
                    var res = {};
                    res[name] = value;
                    return res;
                }
            };

            var res = {
                list: elements,

                dom: function() {
                    return (elements.length == 0 ? null : elements[0]);
                },

                find: function(selector) {
                    var result = [];

                    lib.each(elements, function(element) {
                        result = result.concat(element.querySelectorAll(selector));
                    });

                    return lib.query(result);
                },

                on: function(type, callback) {
                    lib.each(elements, function(element) {
                        element['on' + type] = callback;
                    });

                    return res;
                },

                hasClass: function(className) {
                    if (elements.length == 0) {
                        return false;
                    } else {
                        return ((' ' + elements[0].className + ' ').match(new RegExp(' ' + lib.quoteRegExp(className) + ' ')) != null);
                    }
                },

                addClass: function(className) {
                    lib.each(elements, function(element) {
                        if (!(' ' + element.className + ' ').match(new RegExp(' ' + lib.quoteRegExp(className) + ' '))) {
                            element.className += ' ' + className;
                        }
                    });

                    return res;
                },

                removeClass: function(className) {
                    lib.each(elements, function(element) {
                        element.className = (' ' + element.className + ' ')
                            .replace(/ /g, '  ')
                            .replace(new RegExp(' ' + lib.quoteRegExp(className) + ' '), '')
                            .replace(/[ ]{2,}/, ' ')
                            .replace(/^[ ]+/, '')
                            .replace(/[ ]+$/, '');
                    });

                    return res;
                },

                addRemoveClass: function(value, className) {
                    if (value) {
                        return res.addClass(className);
                    } else {
                        return res.removeClass(className);
                    }
                },

                toggleClass: function(className) {
                    if (res.hasClass(className)) {
                        res.removeClass(className);
                    } else {
                        res.addClass(className);
                    }
                },

                set: function(name, value) {
                    var properties = makeProperties(name, value);

                    lib.each(elements, function(element) {
                        lib.eachKey(properties, function(k, v) {
                            element[k] = v;
                        });
                    });

                    return res;
                },

                get: function(name, def) {
                    if (elements.length == 0) {
                        return (def || null);
                    } else {
                        return (typeof(elements[0][name]) == 'undefined' ? (def || null) : elements[0][name]);
                    }
                },

                setStyle: function(name, value) {
                    var properties = makeProperties(name, value);

                    lib.each(elements, function(element) {
                        lib.eachKey(properties, function(k, v) {
                            element.style[k] = v;
                        });
                    });

                    return res;
                },

                getStyle: function(name, def) {
                    if (elements.length == 0) {
                        return (def || null);
                    } else {
                        return (typeof(elements[0][name]) == 'undefined' ? (def || null) : elements[0][name]);
                    }
                },

                setAttribute: function(name, value) {
                    var properties = makeProperties(name, value);

                    lib.each(elements, function(element) {
                        lib.eachKey(properties, function(k, v) {
                            element.setAttribute(k, v);
                        });
                    });

                    return res;
                },

                getAttribute: function(name, def) {
                    if (elements.length == 0) {
                        return (def || null);
                    } else {
                        return (typeof(elements[0][name]) == 'undefined' ? (elements[0].getAttribute(name) || def || null) : elements[0][name]);
                    }
                },

                exec: function(funcName, args) {
                    lib.each(elements, function(element) {
                        if (typeof(element[funcName]) == 'function') {
                            element[funcName].apply(element, args || []);
                        }
                    });

                    return res;
                },

                hide: function() {
                    lib.each(elements, function(element) {
                        element.style.display = 'none';
                    });

                    return res;
                },

                show: function() {
                    lib.each(elements, function(element) {
                        element.style.display = '';
                    });

                    return res;
                },

                toggle: function(display) {
                    lib.each(elements, function(element) {
                        element.style.display = (display ? '' : 'none');
                    });

                    return res;
                },

                each: function(callback) {
                    lib.each(elements, callback);
                    return res;
                }
            };

            return res;
        },

        toggleLoader: function(mask, visible) {
            if (visible) {
                if (!(loaderMask & mask)) {
                    lib.query('.ajax-loader').show();
                }

                loaderMask |= mask;
            } else {
                loaderMask &= ~mask;

                if (!loaderMask) {
                    lib.query('.ajax-loader').hide();
                }
            }
        },

        // url: url (required)
        // method: GET or POST ("POST" by default)
        // data: data to send (null by default)
        // callback: callback function (if no callback is given, request will be not async)
        // contentType: content type when data is not null ("application/json" by default)
        // jsonRequest: true by default
        // jsonResponse: true by default
        ajax: function(opts, tryNum) {
            if (opts.callback && ajaxIsActive && !tryNum) {
                ajaxQueue.push(opts);
                return;
            }

            var data = null;
            var req = getXMLHttp();

            if (req === null) {
                return null;
            }

            if (!tryNum) {
                tryNum = 1;
            }

            if (!ajaxIsActive && opts.callback) {
                ajaxIsActive = true;
                lib.toggleLoader(lib.LOADER_MASK_AJAX, true);
            }

            req.open(opts.method || 'POST', opts.url, opts.callback ? true : false);

            if (typeof(opts.data) != 'undefined') {
                if (typeof(opts.jsonRequest) == 'undefined' || opts.jsonRequest) {
                    data = JSON.stringify(opts.data);
                } else {
                    data = opts.data;
                }
            }

            try {
                if (window.SERVER_AUTH) {
                    req.setRequestHeader('Authorization', window.SERVER_AUTH);
                    req.withCredentials = true;
                }

                if (data !== null && opts.contentType !== false) {
                    req.setRequestHeader('Content-type', opts.contentType || 'application/json');
                }

                req.send(data);
            } catch (ex) {
                if (console && console.log) {
                    console.log(ex);
                }

                alert("Ajax error: can't send");
            }

            if (opts.callback) {
                req.onreadystatechange = function() {
                    if (req.readyState == 4) {
                        if (ajaxQueue.length != 0) {
                            lib.ajax(ajaxQueue.shift(), 1);
                        } else {
                            lib.toggleLoader(lib.LOADER_MASK_AJAX, false);
                            ajaxIsActive = false;
                        }

                        try {
                            if (req.status == 200 || req.status == 304) {
                                if (typeof(opts.jsonResponse) == 'undefined' || opts.jsonResponse) {
                                    var data = null;

                                    try {
                                        data = JSON.parse(req.responseText);
                                    } catch (ex) {
                                        if (console && console.log) {
                                            console.log('Ajax error: invalid JSON');
                                            console.log(req.responseText);
                                        }
                                    }

                                    try {
                                        opts.callback(data);
                                    } catch (ex) {
                                        if (console && console.log) {
                                            console.log(ex);
                                        }

                                        alert('Ajax error: exception in callback function (1)');
                                    }
                                } else {
                                    opts.callback(req.responseText);
                                }
                            } else {
                                opts.callback(null);
                            }
                        } catch (ex) {
                            if (console && console.log) {
                                console.log(ex);
                            }

                            alert('Ajax error: exception in callback function (2)');
                        }
                    }
                }

                return null;
            }

            try {
                if (req.status != 200 && req.status != 304) {
                    if (tryNum < AJAX_MAX_TRIES) {
                        lib.ajax(opts, tryNum + 1);
                    } else {
                        if (console && console.log) {
                            console.log(req.status);
                            console.log(req.responseText);
                        }

                        alert('Ajax error: too much tries');
                    }
                } else {
                    return req.responseText;
                }
            } catch (ex) {
                if (console && console.log) {
                    console.log(ex);
                }

                alert('Ajax error: mystic');
            }

            return null;
        }
    };
})();

var app = (function() {
    'use strict';

    function getTimestamp() {
        return (new Date()).valueOf();
    }

    function initCommon() {
        document.onselectstart = function(event) {
            return false;
        };

        document.ondragstart = function(event) {
            return false;
        };
    }

    var RTIME = getTimestamp();

    function append(name, path, type, list) {
        var buffer = [
            '<div class="group group--collapsed"><div class="group__name js-toggle">',
            lib.htmlEscape(name),
            '</div>'
        ];

        for (var i = 0, lenI = list.length; i < lenI; i++) {
            buffer.push('<div class="group__content">');
            var subList = list[i];

            for (var j = 0, lenJ = subList.length; j < lenJ; j++) {
                var texName;
                var texType;
                var extOrig;
                var itemClass;

                if (typeof subList[j] === 'object') {
                    texName = subList[j].name;
                    texType = subList[j].type || type;
                    extOrig = subList[j].extOrig || '.png';
                } else {
                    texName = subList[j];
                    texType = type;
                    extOrig = '.png';
                }

                if (texName.charAt(0) === '+') {
                    itemClass = 'group__item--light';
                    texName = texName.substr(1);
                } else {
                    itemClass = '';
                }

                if (j != 0) {
                    buffer.push(' ');
                }

                buffer.push('<div class="group__item');

                if (itemClass !== '') {
                    buffer.push(' ');
                    buffer.push(itemClass);
                }

                var origGfxPath = GRAPHICS_BASE_URL + '/graphics-orig/' + path + texName + extOrig + '?t=' + RTIME;
                var gfxPath = GRAPHICS_BASE_URL + '/graphics/' + path + texName + '.png?t=' + RTIME;

                buffer.push('">');

                buffer.push('<a target="_blank" class="group__tex group__tex--');
                buffer.push(lib.htmlEscape(texType));
                buffer.push('" href="');
                buffer.push(lib.htmlEscape(origGfxPath));
                buffer.push('"><img class="group__tex-image" src="');
                buffer.push(lib.htmlEscape(origGfxPath));
                buffer.push('" alt="" /></a>');

                buffer.push('<a target="_blank" class="group__tex group__tex--');
                buffer.push(lib.htmlEscape(texType));
                buffer.push(' js-tex-link" href="');
                buffer.push(lib.htmlEscape(gfxPath));
                buffer.push('"><img class="group__tex-image js-tex" src="');
                buffer.push(lib.htmlEscape(gfxPath));
                buffer.push('" alt="" /></a>');

                buffer.push('<label class="group__upload">');
                buffer.push('Сменить');
                buffer.push('<input type="file" class="group__upload-input js-upload" data-path="');
                buffer.push(lib.htmlEscape(path + texName + '.png'));
                buffer.push('" />');
                buffer.push('</label>');

                buffer.push('</div>');
            }

            buffer.push('</div>');
        }

        buffer.push('</div>');
        lib.query('.js-container').dom().innerHTML += buffer.join('');
    }

    return {
        init: function() {
            initCommon();

            append('Стены и ящики', 'set-1/walls/wall_', '1x1', [
                ['01', '43', '+25', '+29', '+32', '+33'],
                ['06', '40', '+07', '+34', '+35'],
                ['42', '11', '+12', '+20', '+21'],
                ['05', '41', '+26', '+27', '+28', '18', '19'],
                ['08', '+09', '+10', '+30', '+31'],
                ['+22', '+23', '+24'],
                ['03', '02', '15', '14', '13'],
                ['36', '37', '38', '39'],
                ['16', '17', '04', '44']
            ]);

            append('Окна', 'set-1/twind/twind_', '1x1', [
                ['01', '02', '03', '04', '05', '06', '07', '08']
            ]);

            append('Решётки', 'set-1/twall/twall_', '1x1', [
                ['01', '02', '03'],
                ['04', '05'],
                ['06', '07', '08', '09']
            ]);

            append('Двери', 'set-1/doors/door_', '1x1', [
                ['01_f', '01_s'],
                ['02_f', '02_s'],
                ['03_f', '03_s'],
                ['04_f', '04_s'],
                ['05_f', '05_s'],
                ['06_f', '06_s'],
                ['07_f', '07_s'],
                ['08_f', '08_s']
            ]);

            append('Полы', 'set-1/floor/floor_', '1x1', [
                ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10']
            ]);

            append('Потолки', 'set-1/ceil/ceil_', '1x1', [
                ['01', '+02'],
                ['03', '+04'],
                ['05', '+06']
            ]);

            append('Стрелки (на полу)', 'common/misc/arrow_', '1x1', [
                ['+01', '+02', '+03', '+04']
            ]);

            append('Колонны и прочие непроходимые объекты', 'set-1/ditem/ditem_', '1x1', [
                ['04', '07', '06', '09', '03', '05'],
                ['+01', '+10', '+02', '+08']
            ]);

            append('Лампочки и прочие проходимые объекты', 'set-1/dlamp/dlamp_', '1x1', [
                ['+01'],
                ['02', '03', '04', '05', '06']
            ]);

            append('Объекты', 'common/objects/obj_', '1x1', [
                ['05', '06', '01', '02', '11'],
                ['07', '08', '09', '10', '17', '18'],
                ['03', '04', '13', '19'],
                ['16', '12', '14', '15']
            ]);

            append('Патроны', 'common/bullets/bull_', '1x1', [
                ['1_a1', '1_a2', '1_a3', '1_a4', '1_b1', '1_b2', '1_b3']
            ]);

            append('Иконки (пропорции кнопки "Upgrade" - 4:1)', 'common/icons/', '1x1', [
                ['icon_health', 'icon_ammo', 'icon_armor', 'icon_blue_key', 'icon_red_key', 'icon_green_key'],
                ['icon_map', 'icon_menu', 'icon_shoot'],
                [{ type: '2x2', name: 'back_joy' }, 'icon_joy'],
                [{ type: '2x2', name: 'btn_restart' }, { type: '2x2', name: 'btn_continue' }]
            ]);

            append('Оружия (пропорции - 2:1)', 'common/hit/hit_', '2x1', [
                ['knife_1', 'knife_2', 'knife_3', 'knife_4', 'knife_5', 'knife_6', 'knife_7', 'knife_8'],
                ['pist_1', 'pist_2', 'pist_3', 'pist_4', 'pist_5', 'pist_6', 'pist_7', 'pist_8'],
                ['dblpist_1', 'dblpist_2', 'dblpist_3', 'dblpist_4', 'dblpist_5', 'dblpist_6', 'dblpist_7', 'dblpist_8'],
                ['shtg_1', 'shtg_2', 'shtg_3', 'shtg_4', 'shtg_5', 'shtg_6', 'shtg_7', 'shtg_8'],
                ['ak47_1', 'ak47_2', 'ak47_3', 'ak47_4', 'ak47_5', 'ak47_6', 'ak47_7', 'ak47_8'],
                ['tmp_1', 'tmp_2', 'tmp_3', 'tmp_4', 'tmp_5', 'tmp_6', 'tmp_7', 'tmp_8'],
                ['rocket_1', 'rocket_2', 'rocket_3', 'rocket_4', 'rocket_5', 'rocket_6', 'rocket_7', 'rocket_8'],
            ]);

            append('Иконки оружий', 'drawable/weapon_', '1x1', [
                ['knife', 'pist', 'dblpist', 'shtg', 'ak47', 'tmp', 'rocket']
            ]);

            append('Прочие текстуры', 'drawable/', '1x1', [
                ['ic_launcher', 'sky_1', 'tex_loading']
            ]);

            append('Кнопки', 'drawable-misc/', '1x1', [
                ['btn_options_normal', 'btn_options_pressed'],
                ['btn_like_vk_normal', 'btn_like_vk_pressed'],
                ['btn_like_facebook_normal', 'btn_like_facebook_pressed'],
                ['btn_like_telegram_normal', 'btn_like_telegram_pressed'],
                ['btn_achievements_normal', 'btn_achievements_pressed'],
                ['ic_like_normal', 'ic_like_pressed'],
                ['ic_dislike_normal', 'ic_dislike_pressed'],
                ['button_normal.9', 'button_pressed.9', 'button_disabled.9']
            ]);

            append('Плашка под кнопку "play" (327x314)', 'drawable-misc/', '327x314', [
                ['plate']
            ]);

            append('Кнопка "play" (пропорции - 13:7)', 'drawable-misc/', '13x7', [
                ['btn_play_normal_en', 'btn_play_pressed_en'],
                ['btn_play_normal_ru', 'btn_play_pressed_ru']
            ]);

            append('Задники (пропорции - 5:3)', 'drawable-misc/', '5x3', [
                ['back_splash', { name: 'back_common', extOrig: '.jpg' }]
            ]);

            append('Миникарта (пропорции - 4:3)', 'drawable-misc/', '40x30', [
                ['map_cell_hl', 'map_cell']
            ]);

            append('Горизонтальное соединение для миникарты (пропорции - 8:3)', 'drawable-misc/', '8x3', [
                ['map_conn_hor']
            ]);

            append('Вертикальное соединение для миникарты (пропорции - 2:3)', 'drawable-misc/', '2x3', [
                ['map_conn_vert']
            ]);

            append('Враги', 'common/monsters/mon_', '1x1', [
                ['01_a1', '01_b1', '01_c1'],
                ['01_a2', '01_b2', '01_c2'],
                ['01_a3', '01_b3', '01_c3'],
                ['01_a4', '01_b4', '01_c4'],
                ['01_d1', '01_d2', '01_d3'],
                ['01_e'],

                ['02_a1', '02_b1', '02_c1'],
                ['02_a2', '02_b2', '02_c2'],
                ['02_a3', '02_b3', '02_c3'],
                ['02_a4', '02_b4', '02_c4'],
                ['02_d1', '02_d2', '02_d3'],
                ['02_e'],

                ['03_a1', '03_b1', '03_c1'],
                ['03_a2', '03_b2', '03_c2'],
                ['03_a3', '03_b3', '03_c3'],
                ['03_a4', '03_b4', '03_c4'],
                ['03_d1', '03_d2', '03_d3'],
                ['03_e'],

                ['04_a1', '04_b1', '04_c1'],
                ['04_a2', '04_b2', '04_c2'],
                ['04_a3', '04_b3', '04_c3'],
                ['04_a4', '04_b4', '04_c4'],
                ['04_d1', '04_d2', '04_d3'],
                ['04_e'],

                ['05_a1', '05_b1', '05_c1'],
                ['05_a2', '05_b2', '05_c2'],
                ['05_a3', '05_b3', '05_c3'],
                ['05_a4', '05_b4', '05_c4'],
                ['05_d1', '05_d2', '05_d3'],
                ['05_e']
            ]);

            lib.query('.js-theme').on('mousedown', function() {
                document.body.className = 'theme--' + lib.query(this).getAttribute('data-theme');
            });

            lib.query('.js-toggle').on('mousedown', function() {
                lib.query(this.parentNode).toggleClass('group--collapsed');
            });

            lib.query('.js-upload').on('change', function() {
                if (!this.files.length) {
                    return;
                }

                var file = this.files[0];

                if (!file.name.length) {
                    return;
                }

                if (!file.name.match(/\.(?:gif|png|jpg|jpeg)$/i)
                    || (
                        file.type !== 'image/gif'
                        && file.type !== 'image/png'
                        && file.type !== 'image/jpg'
                        && file.type !== 'image/jpeg'
                    )
                ) {
                    this.value = '';
                    alert('Можно загружать только .gif, .png, .jpg и .jpeg');
                    return;
                }

                if (file.size > 1024 * 1024 * 10) {
                    this.value = '';
                    alert('Файл больше чем 10 мб');
                    return;
                }

                var formData = new FormData();
                formData.append('path', lib.query(this).getAttribute('data-path'));
                formData.append('texture', this.files[0]);

                var linkElement = lib.query(this.parentNode.parentNode).find('.js-tex-link').get(0);

                var imageElement = lib.query(this.parentNode.parentNode).find('.js-tex').get(0);
                var imageSrc = imageElement.src.replace(/\?.*$/, '');
                imageElement.src = 'about:blank';

                var that = this;

                lib.ajax({
                    url: SERVER_URL,
                    data: formData,
                    jsonRequest: false,
                    contentType: false,
                    callback: function (data) {
                        var ts = getTimestamp();
                        that.value = '';

                        linkElement.href = imageSrc + '?t=' + ts;
                        imageElement.src = imageSrc + '?t=' + ts;

                        if (!data) {
                            alert('Произошла какая-то ошибка');
                        } else if (data.error !== null) {
                            alert(data.error);
                        }
                    }
                });
            });
        }
    };
})();

setTimeout(function() {
    app.init();
}, 0);
