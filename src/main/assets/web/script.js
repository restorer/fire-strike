var parts = location.search.replace(/\?/, '').split('&');
var params = {};

for (var i = 0; i < parts.length; i++) {
    var pair = parts[i].split(/=/);

    params[decodeURIComponent(pair[0])] = decodeURIComponent((pair[1] || '').replace(/\+/g, ' '))
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

var appName = (params['appName'] || 'xxxx');
var ver = (params['ver'] || 'x.x');

document.write('<h1 class="nopad">' + appName + '</h1>');
document.write('<p class="botpad">ver. ' + ver + '</p>');
