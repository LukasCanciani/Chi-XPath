/**
 * Created by ElasticAd. Version: 2.2.094
 *
 */

(function (window, document) {
    var eaNative;
    var win = window;
    var doc = document;

    if (isInIframe()) {
        win = window.parent;
        doc = window.parent.document;
    }

    if ((!win.ean || !win.ean.loaded) || win.ean.qneOverwritten) {
        win.ean = win.ean || {};
        win.ean.placements = {};
        win.ean.isIntegrationDSP = [];
        win.ean.dspLoadExtras = [];
        win.ean.placementsByAPXID = {};
        win.ean.placementsNotVisible = [];
        win.ean.videosOnAutoPlay = [];
        win.ean.processedPlacements = {};
        win.ean.placementsById = {};
        win.ean.nativeAds = {};
        win.ean.nativeAdsSource = {};
        win.ean.nativeAdsSourceByVid = {};
        win.ean.articlePlacement = {};
        win.ean.initialized = false;
        win.ean.loaded = false;
        win.ean.ean_window_ready = false;
        win.ean.adUnits = [];
        win.ean.iframes = {};

        win.ean.qneOverwritten = false;

        win.ean.monthNames = {
            en: ["January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"],
            fr: ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"],
            it: ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
                "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"]
        };
        if (!win.ean.prebid_que) {
            win.ean.prebid_que = [];
        }
    } else {
        return win.ean;
    }
    /****************************/
    /*** IE7 querySelectorAll ***/
    /****************************/

    if (typeof doc.querySelector === "undefined") {
        doc.querySelectorAll = function (sel) {

            var sels = sel.split(","),
                    run = function (node, selector) {
                        var sel = selector.split(/[ >]+/), com = selector.match(/[ >]+/g) || [], s, c, ret = [node], nodes, l, i, subs, m, j, check, x, w, ok,
                                as;
                        com.unshift(" ");
                        while (s = sel.shift()) {
                            c = com.shift();
                            if (c)
                                c = c.replace(/^ +| +$/g, "");
                            nodes = ret.slice(0);
                            ret = [];
                            l = nodes.length;
                            subs = s.match(/[#.[]?[a-z_-]+(?:='[^']+'|="[^"]+")?]?/gi);
                            m = subs.length;
                            for (i = 0; i < l; i++) {
                                if (subs[0].charAt(0) == "#")
                                    ret = [doc.getElementById(subs[0].substr(1))];
                                else {
                                    check = c == ">" ? nodes[i].children : nodes[i].getElementsByTagName("*");
                                    if (!check)
                                        continue;
                                    w = check.length;
                                    for (x = 0; x < w; x++) {
                                        ok = true;
                                        for (j = 0; j < m; j++) {
                                            switch (subs[j].charAt(0)) {
                                                case ".":
                                                    if (!check[x].className.match(new RegExp("\\b" + subs[j].substr(1) + "\\b")))
                                                        ok = false;
                                                    break;
                                                case "[":
                                                    as = subs[j].substr(1, subs[j].length - 2).split("=");
                                                    if (!check[x].getAttribute(as[0]))
                                                        ok = false;
                                                    else if (as[1]) {
                                                        as[1] = as[1].replace(/^['"]|['"]$/g, "");
                                                        if (check[x].getAttribute(as[0]) != as[1])
                                                            ok = false;
                                                    }
                                                    break;
                                                default:
                                                    if (check[x].tagName.toLowerCase() != subs[j].toLowerCase())
                                                        ok = false;
                                                    break;
                                            }
                                            if (!ok)
                                                break;
                                        }
                                        if (ok)
                                            ret.push(check[x]);
                                    }
                                }
                            }
                        }
                        return ret;
                    }, l = sels.length, i, ret = [], tmp, m, j;
            for (i = 0; i < l; i++) {
                tmp = run(this, sels[i]);
                m = tmp.length;
                for (j = 0; j < m; j++) {
                    ret.push(tmp[j]);
                }
            }
            return ret;
        };
        doc.querySelector = function (sel) {
            var ret = this.querySelectorAll(sel);
            if (ret.length > 0)
                return ret[0];
            else
                return null;
        };
        if (typeof HTMLElement != "undefined") {
            HTMLElement.prototype.querySelector = doc.querySelector;
            HTMLElement.prototype.querySelectorAll = doc.querySelectorAll;
        } else {
            var dommods_extend = [];
            dommods_extend.push(function () {
                var a = doc.getElementsByTagName("*"), l = a.length, i;
                for (i = 0; i < l; i++) {
                    a[i].querySelector = doc.querySelector;
                    a[i].querySelectorAll = doc.querySelectorAll;
                }
            });
            // dommods_extend is an array of functions that are run whenever the DOM is updated,
            // to apply changes such as auto-resizing textareas, default value for <select> and so on.
        }
    }
    /****************************/
    //utils --------
    win.ean.utils = {
        extend: function (obj, source) {
            if (obj && source) {
                for (var prop in source) {
                    if (source.hasOwnProperty(prop)) {
                        obj[prop] = source[prop];
                    }
                }
            }
            return obj;
        },
        isEmpty: function (obj) {

            // null and undefined are "empty"
            if (obj == null)
                return true;

            // Assume if it has a length property with a non-zero value
            // that that property is correct.
            if (obj.length > 0)
                return false;
            if (obj.length === 0)
                return true;

            // Otherwise, does it have any properties of its own?
            // Note that this doesn't handle
            // toString and valueOf enumeration bugs in IE < 9
            for (var key in obj) {
                if (Object.prototype.hasOwnProperty.call(obj, key))
                    return false;
            }

            return true;
        },
        parseContent: function (content) {
            var myRegExp = new RegExp("<script", "g");
            content = content.replace(myRegExp, "<script data-eascript=\"true\"");
            return content;
        },
        strip: function (value, maxChar) {
            var originalValue = value;
            var originalWords = originalValue.split(" ");
            if (maxChar != undefined && !isNaN(maxChar))
                if (String(value).length > Number(maxChar)) {
                    value = String(value).substr(0, maxChar);
                    value = String(value).split(" ");
                    if (value[value.length - 1] != originalWords[value.length - 1] && value.length > 1) {
                        value.pop();
                    }
                    value = value.join(" ");
                    value += "...";
                }
            return value;
        },
        checkSelectors: function (arr_selectors, allowed, operator) {
            var isAllowed = operator === "and";
            for (var i = 0; i < arr_selectors.length; i++) {
                var isSelector = win.document.querySelector(arr_selectors[i]) ? allowed : !allowed;
                if (operator === "and") {
                    isAllowed = isAllowed && isSelector;
                } else {
                    isAllowed = isAllowed || isSelector;
                    if (allowed === false && isAllowed === true) {
                        break;
                    }
                }
            }
            return isAllowed;
        },
        checkAllowedURLs: function (arr_urls, allowed, strict) {
            var isAllowed = !allowed;
            var path = win.location.href;
            for (var i in arr_urls) {
                if (strict == true) {
                    path = win.location.hostname + win.location.pathname;
                    if (path == arr_urls[i] || win.location.protocol + "//" + path == arr_urls[i]) {
                        isAllowed = allowed;
                        break;
                    }
                } else if (path.indexOf(arr_urls[i]) > -1) {
                    isAllowed = allowed;
                    break;
                }
            }
            return isAllowed;
        },
        checkStatus: function (template) {
            var isAllowed = true;
            var pidOnly = win.ean.utils.getParameterByName('ean-pid-only');
            if (pidOnly && template.pid != pidOnly) {
                return false;
            }
            var tpl = (template && template.template) ? template.template : null;
            if (tpl) {
                if (tpl.strict_urls) {
                    isAllowed = win.ean.utils.checkAllowedURLs(tpl.strict_urls, true, true);
                }
                if (isAllowed && tpl.strict_blocked_urls) {
                    isAllowed = win.ean.utils.checkAllowedURLs(tpl.strict_blocked_urls, false, true);
                }
                if (isAllowed && tpl.allowed_urls) {
                    isAllowed = win.ean.utils.checkAllowedURLs(tpl.allowed_urls, true, false);
                }
                if (isAllowed && tpl.blocked_urls) {
                    isAllowed = win.ean.utils.checkAllowedURLs(tpl.blocked_urls, false, false);
                }
                if (isAllowed && tpl.allowed_selectors) {
                    isAllowed = win.ean.utils.checkSelectors(tpl.allowed_selectors, true, tpl.allowed_selectors_operator || "and");
                }
                if (isAllowed && tpl.blocked_selectors) {
                    isAllowed = win.ean.utils.checkSelectors(tpl.blocked_selectors, false, tpl.blocked_selectors_operator || "and");
                }
                if (isAllowed && tpl.allow_mobile === false && win.ean.utils.isMobile()) {
                    isAllowed = false;
                }
                if (isAllowed && tpl.allow_desktop === false && (!win.ean.utils.isMobile())) {
                    isAllowed = false;
                }
            }
            return template && isAllowed && template.status && (template.status == "active" || template.status == "ready_for_test_campaign" || (template.status == "ready_to_test" && (win.ean.utils.getParameterByName("ean-test-native") || win.ean.utils.getParameterByName("ean-testall-native"))) || (template.status == "pending" && win.ean.utils.getParameterByName("ean-testall-native")));
        },
        replaceHtml: function (rawHtml, settings, selector, method, placementData, templateData) {

            var tags = settings.tags;

            var position = templateData.position;
            var maxChars = templateData.max_chars;
            var selectorIndex = templateData.selector_index || 0;

            for (var i in tags) {
                var maxChar = null;
                if (maxChars && maxChars[i])
                    maxChar = maxChars[i];

                var tagValue = tags[i];

                var content = win.ean.utils.strip(tagValue, maxChar);
                content = win.ean.utils.parseContent(content);
                rawHtml = win.ean.utils.replaceParam(rawHtml, i, content);
            }

            //replace date tag


            var dateExp = function () {
                return /\{ea_date\("(.*?)","(.*?)"(?:,?"?([a-z]{2})?"?)\)\}/g;
            }
            var dateResult = rawHtml.match(dateExp());

            if (dateResult && dateResult.length) {
                for (var d = 0; d < dateResult.length; d++) {

                    var stringToReplace = String(dateResult[d]);
                    var dateMethod = dateExp().exec(stringToReplace);
                    if (dateMethod) {
                        var dateToReplace = win.ean.utils.getEADate(dateMethod [1] || "/", dateMethod [2] || "dmy", dateMethod [3] || "en");
                        rawHtml = rawHtml.replace(stringToReplace, dateToReplace);
                    }
                }
            }

            var container = null;
            if (selector) {
                container = win.document.querySelectorAll(selector)[parseInt(selectorIndex)];
            }
            if (container) {
                var validChildren = eaNative.utils.getValidChildren(templateData.child_selector ? win.document.querySelectorAll(templateData.child_selector) : container.children);

                var beforeElement = validChildren[Math.min(Number(position) || 0, validChildren.length - 1)];

                switch (method) {
                    case "every":

                        var incr = (templateData.frequency || 2);
                        if (!templateData.addedFirst) {
                            incr = 0;
                            templateData.addedFirst = true;
                        }
                        templateData.position = position + incr + 1;
                        position += incr;

                        beforeElement = validChildren[Math.min(position, validChildren.length - 1)];

                    case "insert_before":
                        if (method === 'insert_before') {
                            beforeElement = win.document.querySelectorAll(templateData.before_selector)[templateData.before_selector_index || 0]
                                    || container.children[position || 0];
                        }
                    case "insert_at":
                        var defaultTempElement = "div";

                        var tempElement;
                        switch (container.tagName.toUpperCase()) {
                            default :
                                tempElement = win.document.createElement(defaultTempElement);
                                break;
                            case "TBODY":
                            case "TABLE":
                                tempElement = win.document.createElement("table");
                                win.ean.utils.insertHTML(tempElement, "<tbody></tbody>");
                                tempElement = tempElement.children[0];
                                break;
                            case "TR":
                                tempElement = win.document.createElement("table");
                                win.ean.utils.insertHTML(tempElement, "<tbody><tr></tr></tbody>");

                                tempElement = tempElement.getElementsByTagName("tr")[0];

                                break;
                        }
                        win.ean.utils.insertHTML(tempElement, rawHtml);
                        var firstChild = null;
                        var childrenToInsert = tempElement.children.length;
                        for (i = childrenToInsert - 1; i >= 0; i--) {
                            var childElement = tempElement.children[i];
                            var childClass = childElement.getAttribute('class') || "";
                            childClass += " ean_element";
                            if (i == childrenToInsert - 1 && templateData.ea_native_element_added !== true) {
                                childClass += " ea_native_element ea_apx_pid_" + placementData.placementID;
                                templateData.ea_native_element_added = true;
                            }

                            if(settings.quantx) {
                                childClass += " ea_quantx_pid_" + settings.quantx.iponwebAdUnitId;
                            }

                            childElement.setAttribute("class", childClass);
                            if (position == "last" || container.children.length == 0 || parseInt(position) >= validChildren.length) {
                                container.appendChild(childElement);
                            } else {
                                container.insertBefore(childElement, beforeElement);
                            }

                            if (childElement.nodeName != "STYLE") {
                                firstChild = childElement;
                            }

                        }
                        if (firstChild) {
                            win.ean.utils.triggerEvent("ean-native-placement-show", {container: firstChild, isChild: childrenToInsert > 1, placement: placementData});
                        }
                        break;
                    default :
                        try {
                            var containerClass = container.getAttribute('class') || "";
                            if (templateData.ea_native_element_added !== true) {
                                containerClass += " ea_native_element ea_apx_pid_" + placementData.placementID;
                                templateData.ea_native_element_added = true;
                            }
                            if(settings.quantx) {
                                containerClass += " ea_quantx_pid_" + settings.quantx.iponwebAdUnitId;
                            }

                            container.setAttribute("class", containerClass);

                            win.ean.utils.insertHTML(container, rawHtml);

                            var childrenLen = container.children.length;
                            for (i = 0; i < childrenLen; i++) {
                                var childElement = container.children[i];
                                var childClass = childElement.getAttribute('class') || "";
                                childElement.setAttribute("class", childClass + " ean_element");
                            }

                            win.ean.utils.triggerEvent("ean-native-placement-show", {container: container, isChild: false, placement: placementData});
                        } catch (e) {
                            if (window.console)
                                window.console.log("EANative ERROR: Error inserting HTML", e);
                        }
                        break;
                }

                var excludeIntegrations = ["yahoo"];
                var dspLoadExtrasEl     = container.querySelector('.ean_element') ? container.querySelector('.ean_element') : container;

                if (settings.adnx && eaNative.dspLoadExtras[settings.ah] && settings.adnx.dsp.indexOf(excludeIntegrations) === -1) {
                    eaNative.dspLoadExtras[settings.ah](dspLoadExtrasEl);
                } else if (win.ean.utils.isElementVisiblePercent(dspLoadExtrasEl, 50)
                    && settings.hasOwnProperty('adnx')
                    && settings.adnx.dsp.indexOf(excludeIntegrations) > -1) {
                    setTimeout(function () {
                        eaNative.dspLoadExtras[settings.ah](dspLoadExtrasEl);
                    }, 1000);
                }
            }
        },
        triggerEvent: function (type, data) {
            if (doc.createEvent) {
                var evt = doc.createEvent("Event");
                evt.initEvent(type, true, true);
                evt.data = data;
                evt.eventType = type;
                if (doc.dispatchEvent) {
                    doc.dispatchEvent(evt);
                }
            }
        },
        getElementIndex: function (children, element) {
            for (var i = 0; i < children.length; i++) {
                if (children[i] == element) {
                    return i;
                }
            }
        },
        getEADate: function (splitter, format, language) {
            var z = new Date();
            var m = ((z.getMonth() + 1) > 9) ? (z.getMonth() + 1) : ('0' + (z.getMonth() + 1));
            var d = (z.getDate() > 9) ? z.getDate() : ("0" + z.getDate());
            if (!language || !win.ean.monthNames[language]) {
                language = "en";
            }
            var dateArray = [];
            var formatParts = format.split("");

            if (formatParts) {
                for (var f = 0; f < formatParts.length; f++) {
                    switch (formatParts[f]) {
                        case "d":
                            dateArray.push(d);
                            break;
                        case "m":
                            dateArray.push(m);
                            break;
                        case "y":
                            dateArray.push(z.getFullYear());
                            break;
                        case "F":
                            dateArray.push(win.ean.monthNames[language][z.getMonth()]);
                            break
                        case "K":
                            dateArray.push(win.ean.monthNames[language][z.getMonth()].slice(0, 3));
                            break;
                        case "h":
                            dateArray.push(z.getHours());
                            break;
                        case "i":
                            dateArray.push(z.getMinutes());
                            break;
                        case "s":
                            dateArray.push(z.getSeconds());
                            break;
                    }
                }
            }
            return dateArray.join(splitter);
        },
        replaceParam: function (str, param, content) {
            var myRegExp = new RegExp("{" + param + "}", "g");
            try {
                str = str.replace(myRegExp, content);
            } catch (e) {

            }
            return str;
        },
        loadScript: function (scriptSrc, onLoadCallback, iframeDoc, scriptId) {
            var wDoc = iframeDoc ? iframeDoc.contentWindow.document : win.document;
            var head = iframeDoc ? iframeDoc.contentWindow.document.body : win.document.getElementsByTagName("head")[0];
            var scr = wDoc.createElement("script");
            scr.type = "text/javascript";
            scr.onload = onLoadCallback;

            if(scriptId) {
                scr.setAttribute("id", scriptId);
            }

            scr.async = true;
            scr.src = scriptSrc;
            head.appendChild(scr);
            return scr;
        },
        loadTrackerImage: function(el, iframeDoc, className) {
            var wDoc = iframeDoc ? iframeDoc.contentWindow.document : win.document;
            var body = iframeDoc ? iframeDoc.contentWindow.document.body : win.document.getElementsByTagName('body')[0];
            var img = wDoc.createElement('img');
            img.src = el;
            img.className       = className ? className : 'ean-clicktracker';
            img.style.width     = 0;
            img.style.height    = 0;
            img.style.display   = 'none';
            body.appendChild(img);
            return img;
        },
        loadPlacementConfig: function (baseURL) {
            baseURL = baseURL.replace("{domain}", win.ean.utils.getDomainName());
            win.ean.utils.loadScript(baseURL + "?r=" + win.ean.utils.getRand());
        },
        getCookieByName: function(name) {
            var dc = document.cookie;
            var prefix = name + "=";
            var begin = dc.indexOf("; " + prefix);
            if (begin == -1) {
                begin = dc.indexOf(prefix);
                if (begin != 0) return null;
            } else {
                begin += 2;
                var end = document.cookie.indexOf(";", begin);
                if (end == -1) {
                    end = dc.length;
                }
            }
            return decodeURI(dc.substring(begin + prefix.length, end));
        },
        getParameterByName: function (name) {
            name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
            var regex = new RegExp("[\\?&#]" + name + "=([^&#]*)"),
                    results = regex.exec(win.location.search);
            if (results == null) {
                results = regex.exec(win.location.hash);
            }
            if (results == null) {
                if (win[name] != undefined) {
                    return win[name];
                }
            }
            return results == null
                ? (win.ean.utils.getCookieByName(name) == null) ? "" : win.ean.utils.getCookieByName(name)
                : decodeURIComponent(results[1].replace(/\+/g, " "));
        },
        getDomainName: function () {
            var forcedDomain = eaNative.utils.getParameterByName("ean-domain");
            if (forcedDomain != "") {
                return forcedDomain;
            }
            var domain = win.location.hostname;

            if (domain.substr(0, 4) == "www.") {
                domain = domain.substr(4);
            }

            var tempSplitter = "__ean__";
            for (var i = 0; i < EAdConfig.customDomainExtensions.length; i++) {
                var ext = EAdConfig.customDomainExtensions[i];
                domain = domain.replace(ext, ext.replace(/\./g, tempSplitter));
            }
            var domainParts = domain.split(".");
            domainParts.splice(0, domainParts.length - 2);
            domain = domainParts.join(".");

            var regExp = new RegExp(tempSplitter, "g");
            domain = domain.replace(regExp, ".");

            return domain;
        },
        openURL: function (url, target, clickTag, specs) {
            if (clickTag && clickTag != "")
                url = encodeURI(url);
            url = (clickTag || "") + url;
            target = target || "_self";
            if (specs)
                win.open(url, target, specs);
            else
                win.open(url, target);
        },
        getRand: function () {
            return Math.random().toString(16).substr(2);
        },
        getValueFromPath: function (obj, is, value) {
            if (typeof is == 'string')
                return win.ean.utils.getValueFromPath(obj, is.split('.'), value);
            else if (is.length == 1 && value !== undefined)
                return obj[is[0]] = value;
            else if (is.length == 0)
                return obj;
            else
                return win.ean.utils.getValueFromPath(obj[is[0]], is.slice(1), value);
        },
        isElementVisible: function (selector, method, placement, position)
                //trebuie modificat nu cauta bine visibilitatea elementului, buhuhuhuhuhu
                {
                    var elem = null;
                    var templateData = placement.template;
                    var visibilityOffset = templateData.visibility_offset ? templateData.visibility_offset : 200;
                    if (!position) {
                        position = templateData.position;
                    }
                    var selectorIndex = templateData.selector_index || 0;
                    var container = null;
                    if (selector) {
                        container = win.document.querySelectorAll(selector)[parseInt(selectorIndex)];
                    }
                    var verifyTop = false;

                    if (container) {
                        var validChildren = eaNative.utils.getValidChildren(templateData.child_selector ? win.document.querySelectorAll(templateData.child_selector) : container.children);
                        var beforeElement = validChildren[Math.min(Number(position) || 0, validChildren.length - 1)];
                        switch (method) {
                            case "insert_before":
                                beforeElement = win.document.querySelectorAll(templateData.before_selector)[templateData.before_selector_index || 0]
                                        || container.children[position || 0];
                            case "insert_at":
                                if (method != 'insert_before' && (position == 0 || validChildren.length == 0)) {
                                    elem = container;
                                } else if (position == "last" || parseInt(position) >= validChildren.length - 1) {
                                    elem = validChildren[validChildren.length - 1];
                                } else {
                                    elem = beforeElement;
                                    verifyTop = true;
                                }
                                break;
                            case "every":
                                elem = validChildren[Math.min(position, validChildren.length - 1)];
                                break;
                            default :
                                elem = container;
                                break;
                        }
                    }

                    if (!elem || elem.offsetParent === null) {
                        return false;
                    } else {
                        if (elem === container || method == "update") {
                            verifyTop = true;
                        }
                    }

                    var pageYOffset = win.pageYOffset;
                    if (pageYOffset == undefined) {
                        pageYOffset = doc.documentElement.scrollTop;
                    }
                    var innerHeight = win.innerHeight;
                    if (innerHeight == undefined) {
                        innerHeight = doc.documentElement.clientHeight;
                    }

                    var docViewTop = Math.max(pageYOffset, 0);
                    var docViewBottom = docViewTop + innerHeight;

                    var elemTop = win.ean.utils.getOffset(elem);
                    var elemBottom = elemTop + elem.offsetHeight;

                    var isVisible = false;
                    if (verifyTop == true) {
                        isVisible = (elemTop >= docViewTop - visibilityOffset) && (elemTop < docViewBottom + visibilityOffset);
                    } else {
                        isVisible = (elemBottom >= docViewTop - visibilityOffset && elemBottom < docViewBottom + visibilityOffset);
                    }
                    if (!isVisible) {
                        var bounding = elem.getBoundingClientRect();
                        if (verifyTop == true) {
                            isVisible = (bounding.top > 0 && bounding.top < innerHeight + visibilityOffset);
                        } else {
                            isVisible = bounding.bottom > 0 && bounding.bottom < innerHeight + visibilityOffset;
                        }
                    }
                    return isVisible;
                },
        getOffset: function (elem, side)
        {
            if (!side) {
                side = 'offsetTop';
            }
            var offset = 0;
            do {
                if (!isNaN(elem[side]))
                {
                    offset += elem[side];
                }
            } while (elem = elem.offsetParent);
            return offset;
        },
        isElementVisiblePercent: function (elem, percent) {
            var bounds = elem.getBoundingClientRect();
            var pageYOffset = win.pageYOffset;
            if (pageYOffset === undefined) {
                pageYOffset = doc.documentElement.scrollTop;
            }
            var innerHeight = win.innerHeight;
            if (innerHeight === undefined) {
                innerHeight = doc.documentElement.clientHeight;
            }
            var topBound = bounds.top + (percent ? bounds.height * percent / 100 : 0);
            var bottomBound = bounds.bottom - (percent ? bounds.height * percent / 100 : 0);

            return ((topBound > 0) && (bottomBound < innerHeight));
        },
        replaceDiacritics: function (s) {
            var re1 = /[ÁÀÂÃÄáàâãªä]/g;
            var re2 = /[ÉÈÊËéèêë&]/g;
            var re3 = /[óòôõöºÖÓÒÔÕ]/g;
            var re4 = /[úùûüÚÙÛÜ]/g;
            var re5 = /[íÍîïÏ]/g;
            s = s.replace(re1, "a");
            s = s.replace(re2, "e");
            s = s.replace(re3, "o");
            s = s.replace(re4, "u");
            s = s.replace(re5, "i");
            s = s.replace(/[çÇ]/g, "c");
            return s;
        },
        getStyle: function (oElm, strCssRule) {
            var strValue = "";
            if (document.defaultView && document.defaultView.getComputedStyle) {
                strValue = document.defaultView.getComputedStyle(oElm, "").getPropertyValue(strCssRule);
            } else if (oElm.currentStyle) {
                strCssRule = strCssRule.replace(/\-(\w)/g, function (strMatch, p1) {
                    return p1.toUpperCase();
                });
                strValue = oElm.currentStyle[strCssRule];
            }
            return strValue;
        },
        getValidChildren: function (collection) {
            var validChildren = [];
            if (collection && collection.length > 0) {
                for (var i = 0; i < collection.length; i++) {
                    if (collection[i].nodeType == 1) {
                        validChildren.push(collection[i]);
                    }
                }
            }
            return validChildren;
        },
        isMobile: function () {
            return  /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(win.navigator.userAgent);
        },
        isIOS: function () {
            return  /(iPad|iPhone|iPod)/g.test(win.navigator.userAgent);
        },
        isTablet: function () {
            return Math.min(win.screen.height, win.screen.width) >= 600;
        },
        replaceCreativeMacros: function (source, adSettings) {

            if (adSettings.auctionid != undefined) {
                source = source.replace('${AUCTION_ID}', adSettings.auctionid);
            }
            source = source.replace('${PUBLISHER_ID}', adSettings.pubid);
            source = source.replace('${SITE_ID}', adSettings.sid);
            source = source.replace('${TAG_ID}', adSettings.adnxsId);
            source = source.replace('${PLACEMENT_ID}', adSettings.pid);
            source = source.replace('${CP_ID}', adSettings.adnxsId);
            source = source.replace('${USER_COUNTRY}', adSettings.country);
            source = source.replace('${CLICK_URL}', adSettings.clickTAG);
            source = source.replace('${USER_ID}', adSettings.uid);
            source = source.replace('${SSP_DATA}', adSettings.sspData);
            source = source.replace('${CACHEBUSTER}', win.ean.utils.getRand());
            source = source.replace('${CREATIVE_ID}', adSettings.cid);
            source = source.replace('${CP_CODE}', adSettings.cpCode);
            return source;
        },
        getURLHostname: function (href) {
            var l = doc.createElement("a");
            l.href = href;
            return l.hostname;
        },
        base64url_encode: function (str) {
            return str.replace("/", "_").replace("+", "-");
            ;
        },
        ajaxLoad: function (url, success, error, headers, with_credentials) {
            var xmlhttp;
            var ieVer = getIEVersion();
            var isUnderIE10 = ieVer > 1 && ieVer < 10;
            if (window.XMLHttpRequest && !isUnderIE10) {
                // code for IE7+, Firefox, Chrome, Opera, Safari
                xmlhttp = new XMLHttpRequest();
            } else if (isUnderIE10) {
                xmlhttp = new XDomainRequest();
            } else {
                // code for IE6, IE5
                xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            if (!isUnderIE10) {
                xmlhttp.onreadystatechange = function () {
                    if (xmlhttp.readyState == XMLHttpRequest.DONE) {
                        if (xmlhttp.status == 200) {
                            success(xmlhttp, xmlhttp.responseText);
                        } else {
                            error(xmlhttp, xmlhttp.status);
                        }
                    }
                };
            } else {
                xmlhttp.onerror = function () {
                    error(xmlhttp, xmlhttp.status);
                }
                xmlhttp.onload = function () {
                    success(xmlhttp, xmlhttp.responseText);
                }
            }
            xmlhttp.open("GET", url, true);

            if (with_credentials != null) {
                xmlhttp.withCredentials = true;
            }

            if (headers != null) {
                for (var i in headers) {
                    if (headers.hasOwnProperty(i)) {
                        xmlhttp.setRequestHeader(i, headers[i]);
                    }
                }
            }
            xmlhttp.send();
        },
        isArray: function (obj) {
            if (Array.isArray) {
                return Array.isArray(obj);
            }
            return Object.prototype.toString.call(obj) === '[object Array]';
        },
        insertHTML: function (container, htmlCode) {
            if (container != null) {
                try {
                    container.innerHTML = htmlCode;
                } catch (e) {
                    var ie8Wrap = win.document.createElement('DIV');
                    ie8Wrap.innerHTML = htmlCode;
                    for (var i = 0; i < ie8Wrap.children.length; i++) {
                        container.appendChild(ie8Wrap.children[i]);
                    }
                }
            }
        },
        getParentByClass: function (el, parent) {
            var parentName = parent.toLowerCase();

            while (el && el.parentNode) {
                el = el.parentNode;
                if (el.className.indexOf(parentName) > -1) {
                    return el;
                }
            }

            return null;
        }
    };

    function isInIframe() {
        if (window.parent != window) {
            try {
                var u = window.parent.location;
                var frm = window.frameElement;
                var doc = window.parent.document
                if (doc == undefined)
                    return false;
                return  true;
            } catch (e) {
            }
        }
        return false;
    }
    ;

    if (typeof JSON === "undefined") {
        var script = win.document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://cdn.elasticad.net/native/serve/js/helper/json3.min.gz.js";
        win.document.getElementsByTagName("head")[0].appendChild(script);
    }


    eaNative = win.ean;

    var trackingEvents = {
        'click': 1,
        'general': 100
    }
    var formatType = {
        "native": 2,
        "article": 3,
        "expanded": 4
    }
    var URLPaths = {
        nativePlacementsConfigURL: "{protocol}//{bucket}/native/placements/{domain}/pconfig",
        articlePlacemensConfigURL: "{protocol}//{bucket}/native/placements/{domain}/aconfig",
        vastEmbed: "{protocol}//{v_cdn}/native/serve/vastEmbed.html?vast=",
        vastTeaser: "{protocol}//{v_cdn}/native/serve/vastTeaser.html?vast=",
        prismContentURL: "{protocol}//{prism_serve}/content/index.html?inline=false&hash=",
        prismCanvasJS: "{protocol}//cdn.prism-mob.com/serve/js/CanvasAd.gz.js",
//		vastTeaser:"{protocol}//devfiles.elasticad.net/native/serve/vastTeaser.html?vast=", //doar pentru test
        articleURL: "{protocol}//{bucket}/native/ad/{hash}/article/settings",
        nativeURL: "{protocol}//{bucket}/native/ad/{hash}/teaser/settings",
        cdnURL: "{protocol}//{cdn}",
        sharePageUrl: "{protocol}//{share_page_domain_url}/nredir.php?hash={ah}&href={encoded_article_link}&r=" + win.ean.utils.getRand(),
        defaultSharePageUrl: "{share_page_domain}",
        resizeServerImagesPath: "{protocol}//{subdomain}resize.elasticad.net/{scalemode}-{wxh}/{hash}",
        imagesPath: "{protocol}//{cdnImg}/media/image/{hash}/{scalemode}-{wxh}",
        originalImagesPath: "{protocol}//{cdnImg}/media/image/{hash}/original",
//		imagesPath:"{protocol}//{bucket}/media/image/{hash}/{scalemode}-{wxh}",
//		originalImagesPath:"{protocol}//{bucket}/media/image/{hash}/original",

        externalResizeServerImagesPath: "{protocol}//{subdomain}resize.elasticad.net/{scalemode}-{wxh}/{hash}/external",
        externalImagesPath: "{protocol}//{cdnImg}/media/extimage/{hash}/{scalemode}-{wxh}",
        externalOriginalImagesPath: "{protocol}//{cdnImg}/media/extimage/{hash}/original",
//        externalImagesPath:"{protocol}//{bucket}/media/extimage/{hash}/{scalemode}-{wxh}",
//        externalOriginalImagesPath:"{protocol}//{bucket}/media/extimage/{hash}/original",

        trackingURL: "{protocol}//{subdomain}tracking.elasticad.net/pixel.php",
        integrationHelperURL: "{protocol}//{v_cdn}/native/serve/js/helper/integration/{dsp}_v2.gz.js",
        quantxNativeEmbedURL: "{protocol}//{cdn}/native/serve/js/quantx/nativeEmbed.gz.js"
    };

    var defaultImageScaleMode = {
        brand_logo: "scaleh",
        defaultScale: "scalecrop"
    };
    eaNative.window = win;
    eaNative.document = doc;


    //var bucket = "s3.amazonaws.com/files.elasticad.net";
    var bucket = "de5zarwna0j2q.cloudfront.net";
    var cdn = "cdn.elasticad.net";
    var v_cdn = cdn;
    var prism_serve = "cdn.prism-mob.com";
    var cdnImg = "de5zarwna0j2q.cloudfront.net";
    var subdomain = "";
    var protocol = "http:";
    var adnxsURL = "http://cdn.adnxs.com/";
    var sharePageDomain = "share.elasticad.net";
    var defaultPlacementId = "10761294";
    var queryParams = {};
    var ttjURI = 'ib.adnxs.com/ttj';
    var isDev = false;
    var isGLive = false;

    if (win.ean.utils.getParameterByName("ean-dev") === "true")
        isDev = true;
    if (win.location.protocol == "https:")
        protocol = win.location.protocol;


    var script = window.document.getElementById('ean-native-embed-tag');
    if (script) {
        var loadedScriptSource = script.getAttribute("src");
        if (loadedScriptSource && loadedScriptSource.indexOf("devfiles.elasticad.net") != -1) {
            isDev = true;
        } else if (loadedScriptSource && (loadedScriptSource.indexOf("gcdn.elasticad.net") != -1)) {
            isGLive = true;
        }

        if (loadedScriptSource.indexOf("?") > 0) {
            var jsQuery = loadedScriptSource.substr(loadedScriptSource.indexOf("?") + 1);

            eaNative.nativeEmbedScriptQuery = jsQuery;

            var jsQueryParams = jsQuery.split("&");
            for (var i = 0; i < jsQueryParams.length; i++) {
                var item = jsQueryParams[i].split("=");
                queryParams[item[0]] = item[1];
            }
        }
    }

    if (isDev) {
        subdomain = "dev.";
        v_cdn = cdn = bucket = "devfiles.elasticad.net";
        prism_serve = "devfiles.contad.theadtech.com";
        cdnImg = "d1ny4qngmmwofe.cloudfront.net";
        //defaultPlacementId = "1436048";
        sharePageDomain = "share.sharepage.dev.elasticad.net";
        if (!queryParams["liveSSP"]) {
            ttjURI = 'ib.client-testing.adnxs.net/ttj';
        }
        URLPaths.prismCanvasJS = "{protocol}//devfiles.contad.theadtech.com.s3.amazonaws.com/serve/js/CanvasAd.gz.js";
        URLPaths.quantxNativeEmbedURL = "{protocol}//devfilesquantx.theadtech.com.s3.amazonaws.com/serve/js/nativeEmbed.js";
    } else if (isGLive == true) {
        subdomain = "g";
        cdn = bucket = "gpfiles.elasticad.net";
        v_cdn = cdnImg = "storage.googleapis.com/gcdn.elasticad.net";

    }

    if (protocol == "https:") {
        adnxsURL = "https://a248.e.akamai.net/appnexus.download.akamai.com/89298/adnexus-prod/";
        ttjURI = "secure.adnxs.com/ttj";
    }
    for (var i in URLPaths) {
        URLPaths[i] = URLPaths[i].replace("{bucket}", bucket).replace("{subdomain}", subdomain)
                .replace("{cdn}", cdn)
                .replace("{v_cdn}", v_cdn)
                .replace("{cdnImg}", cdnImg)
                .replace("{protocol}", protocol)
                .replace("{adnxs}", adnxsURL)
                .replace("{prism_serve}", prism_serve)
                .replace("{share_page_domain}", sharePageDomain);
    }
    if (win.ean.utils.getParameterByName('ean-placement-preview') == "true") {
        win.ean.utils.loadScript(protocol + "//" + cdn + "/native/serve/js/helper/nativePreviewEmbedHelper.gz.js?v=1.0");
    }
    var EAdConfig = {
        debug: isDev,
        uid: 0,
        vid: 0,
        h: 0,
        defaultPlacementId: defaultPlacementId,
        queryParams: queryParams,
        customDomainExtensions: ['elasticad.net.s3.amazonaws.com', "elasticad.net", 'wordpress.com',
            'elasticad.jumpeye.net', 'co.uk', 'elasticad.jumpeye.com', 'blogspot.ro', 'blogspot.com'],
        nativeAdSizes: {
            ttj: {
                size: "19x11",
                promo_sizes: "19x12,608x226,608x227"
            },
            ast: {
                sizes: [[19, 11], [19, 12], [608, 226], [608, 227], [1, 1]]
            }
        },
        sspTAG: win.ean.utils.getParameterByName('ean-test-ast') == "true" ? "ast" : "ttj",
        prebid: queryParams.prebid === "true" && win.ean.utils.getParameterByName('ean-skip-prebid') !== "true"
    }
    if (win.ean.utils.getParameterByName('ean-debug')) {
        EAdConfig.debug = true;
    }
    eaNative.URLPaths = URLPaths;
    eaNative.EAdConfig = EAdConfig;

    eaNative.getURLPath = function (pathName, dsp) {
        switch (pathName) {
            case "imagesPath":
            case "originalImagesPath":
            case "resizeServerImagesPath":
                switch (dsp) {
                    case "a":
                        return URLPaths[pathName];
                        break;
                    default :
                        return URLPaths["external" + pathName.charAt(0).toUpperCase() + pathName.slice(1)];
                        break;
                }
                break;
            default :
                return URLPaths[pathName];
                break;
        }
        return '';
    };
    eaNative.loadDataInIFrame = function (content, closeDocAtEnd, saveIframe) {
        var frmEm = doc.createElement("iframe");
        var frameId = "load-ad-" + win.ean.utils.getRand();
        frmEm.id = frameId;

        var iframeContainer = doc.createElement("div");
        iframeContainer.style.display = "none";
        iframeContainer.appendChild(frmEm);

        if (frmEm) {
            //added onload event fix to 2.1.004
            frmEm.src = 'about:blank';
            frmEm.onload = function () {
                var frmCnt = (frmEm.contentWindow) ? frmEm.contentWindow : (frmEm.contentDocument.document) ?
                        frmEm.contentDocument.document : frmEm.contentDocument;
                frmCnt.document.open();
                frmCnt.document.write(content);
                if (closeDocAtEnd !== false) {
                    frmCnt.document.close();
                }
                if (saveIframe) {
                    win.ean.iframes[saveIframe] = frmEm;
                }
                frmEm.onload = null;
            };

        }
        doc.body.appendChild(iframeContainer);
    };
    eaNative.loadLandingPageInIFrame = function (lp) {
        var frmEm = doc.createElement("iframe");
        var frameId = "lp-" + win.ean.utils.getRand();
        frmEm.id = frameId;

        var iframeContainer = doc.createElement("div");
        iframeContainer.style.visibility = "hidden";
        iframeContainer.style.position = "absolute";
        iframeContainer.style.left = "-10000px";
        iframeContainer.style.top = "-10000px";
        iframeContainer.style.top = "-10000px";
        frmEm.width = win.innerWidth;
        frmEm.height = win.innerHeight;
        iframeContainer.appendChild(frmEm);
        frmEm.src = lp;
        doc.body.appendChild(iframeContainer);

    };
    eaNative.defineSPPTag = function (spp_pid, config) {

        if (!EAdConfig.prebid) {
            //if (EAdConfig.sspTAG == 'ttj' && config.allow_ast =='true') {
            //    EAdConfig.sspTAG = 'ast';
            //}
            switch (EAdConfig.sspTAG) {
                case "ttj":
                    win.ean.utils.extend(config, EAdConfig.nativeAdSizes[EAdConfig.sspTAG]);

                    var sspSrc = protocol + "//" + ttjURI + "?";
                    config.id = spp_pid;
                    var params = [];
                    for (var i in config) {
                        params.push(encodeURIComponent(i) + "=" + encodeURIComponent(config[i]));
                    }
                    sspSrc += params.join("&");
                    /*var content = "<html><head><script type='text/javascript'>window.closeDocumentInterval =setInterval(function(){var scripts = document.getElementsByTagName('script');
                     if(scripts.length>2){window.clearInterval(window.closeDocumentInterval);document.close();} }, 500);var newDomain = '" + doc.domain + "'; if(document.domain!=newDomain)document.domain = newDomain; document.write('<script type=text/javascript src=" + sspSrc + "></scr'+'ipt>')</scr" + "ipt></head></html>";
                     */
                    var ieVer = getIEVersion();
                    var isIE = !!(ieVer > -1 && ieVer < 10);
                    var addScript = "";
                    if (isIE) {
                        addScript = "<scr" + "ipt type='text/javascript'>console.log('dsds');window.closeDocumentInterval" +
                                " =setInterval(function(){var scripts =" +
                                " document.getElementsByTagName('script');console.log(scripts.length);" +
                                " if(scripts.length>2){window.clearInterval(window.closeDocumentInterval);document.close();} }, 500);</scr" + "ipt>"
                    }
                    var content = "<html><head></head><body><script type='text/javascript' src='" + sspSrc + "'></scr" + "ipt>" + addScript + "</body></html>";
                    eaNative.iframes[spp_pid] = null;
                    eaNative.loadDataInIFrame(content, !isIE, spp_pid);

                    break;
                case "ast":
                    //create empty apntag object if doesn't exist
                    win.apntag = win.apntag || {};
                    win.apntag.eanq = win.apntag.eanq || [];
                    if (win.apntag.eanq.length==0) {
                        win.apntag = {};
                        win.apntag.eanq = [];
                    }
                    //create a queue on the apntag object
                    if (!win.apntag.l) {
                        win.apntag.l = true;
                        win.ean.utils.loadScript(protocol + '//acdn.adnxs.com/ast/ast.js');

                    }
                    var astConfig = {};
                    win.ean.utils.extend(astConfig, EAdConfig.nativeAdSizes[EAdConfig.sspTAG]);
                    astConfig.tagId = Number(spp_pid);
                    astConfig.invCode = config.ext_inv_code;
                    astConfig.targetId = 'apn_ad_' + astConfig.invCode;
                    astConfig.allowSmallerSizes = false;
                    astConfig.disablePsa = true;
                    astConfig.allowedFormats = ['native', 'banner'];

                    for(var propertyName in config) {
                        if (config[propertyName] == true) {
                            config[propertyName] = "true";
                        }
                        if (config[propertyName] == false) {
                            config[propertyName] = "false";
                        }
                    }

                    astConfig.keywords = config;
                    //console.log('ASTCOnfig:',JSON.stringify(astConfig));
                    win.apntag.eanq.push(astConfig);

                    break;
            }
        } else {
            var preBidConfig = {};
            config.placementId = spp_pid;
            preBidConfig.params = config;
            preBidConfig.bidder = "appnexus";
            eaNative.adUnits.push(preBidConfig);
        }

    }
    eaNative.loadSSPSpots = function () {
        if (!EAdConfig.prebid) {
            //we only need to load for AST tag
            if (EAdConfig.sspTAG == "ast") {
                if (win.apntag && win.apntag.eanq) {
                    win.apntag.anq = win.apntag.anq || [];

                    if (win.apntag.eanq.length>0) {
                    //fix - if we call all tags available once, second tag gets a "member" error
                        for (var i = 0; i < win.apntag.eanq.length; i++) {
                            var config = win.apntag.eanq[i];
                            eaNative.loadASTTag(config);
                        }
                    }
                }
            }
        } else {
            if (eaNative.prebid_que.length > 0) {
                for (var i = 0; i < eaNative.prebid_que.length; i++) {
                    eaNative.prebid_que[i](eaNative.getPreBidAdUnits());
                }
            }
        }
    }
    eaNative.loadASTTag = function (config) {
        win.apntag.anq.push(function () {
            win.apntag.defineTag(config);
            win.apntag.onEvent('adAvailable', "apn_ad_" + config.invCode, function (adData) {
                switch (adData.adType) {
                    case "native":
                        eaNative.parseOpenRTBNativeAd(adData);
                        break;
                    case "banner":
                        if (adData.source == "csm") {
                            var mc = win.apntag.eanq;
                            for (var i = 0; i < mc.length; i++) {
                                if (adData.tagId == mc[i].tagId) {
                                    if (mc[i].ad && mc[i].ad.mediatedContent) {
                                        eaNative.loadDataInIFrame(mc[i].ad.mediatedContent, false, adData.tagId);
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                        eaNative.loadDataInIFrame(adData.banner.content);
                        if (adData.banner.trackers) {
                            var trackers = adData.banner.trackers.concat();
                            while (trackers.length) {
                                var trackingPack = trackers.shift();
                                while (trackingPack && trackingPack.impression_urls && trackingPack.impression_urls.length > 0) {
                                    win.ean.utils.loadScript(trackingPack.impression_urls.shift());
                                }
                            }
                        }
                        break;
                }
            });
            win.apntag.loadTags();
        });
    }
    eaNative.getPreBidAdUnits = function () {
        return eaNative.adUnits;
    }
    eaNative.loadPreBidAd = function (adData) {
        if (adData.hb_ad || adData.hb_adUrl) {
            var adSrc = "";
            if (adData.hb_adUrl) {
                eaNative.utils.ajaxLoad(adData.hb_adUrl, function (request, content) {
                    eaNative.loadDataInIFrame(content);
                }, function (request, status) {

                })
            } else {
                adSrc = "<script type='text/javascript' >" + adData.hb_ad + "</script>";
                var content = "<html><head>" + adSrc + "</head></html>";
                eaNative.loadDataInIFrame(content);
            }

        }
    }
    eaNative.initSSPTag = function (placement) {
        var placementID = placement.placementID;
        var placements = eaNative.placements[placementID] || [];
        var deviceType = 'normal';
        placements.push(placement);
        eaNative.placements[placementID] = placements;
        eaNative.placementsByAPXID[placementID] = placement;
        if (win.ean.utils.isMobile()) {
            deviceType = win.ean.utils.isTablet() ? 'tablet' : 'phone';
        }
        var testHash = win.ean.utils.getParameterByName("ean-test-hash");
        var testDSP = win.ean.utils.getParameterByName("ean-test-dsp");
        if (testHash || testDSP) {
            var dspData = "";
            var type = "native";
            if (testDSP) {
                type = "external";
                switch (testDSP) {
                    case "facebook":
                        dspData = "122123021503789_125684801147611";
                        break;
                    case "criteo":
                        dspData = "//cas.criteo.com/delivery/0.1/napi.json?zoneid=244688&bannerid=3919726";
                        break;
                    case "nextperf":
                        dspData = decodeURIComponent(win.ean.utils.getParameterByName("ean-test-dspData"));
                        break;
                }
            } else {
                testDSP = 'a';
            }
            eaNative.initNativeAd({
                type: type,
                ah: testHash,
                dsp: testDSP,
                dspData: dspData,
                adnxsId: placementID,
                clickTAG: "",
                uid: "0",
                isInPreview: win.ean.utils.getParameterByName("ean-isinpreview")
            });
        } else {
            var invCode = "ea_pid_" + placement.pid;
            var loadConfig = {
                ea_placement_id: placement.pid,
                ea_placement_expandable: placement.expandable || false,
                ea_placement_accept_auto_expandable: placement.accept_auto_expandable != undefined ? placement.accept_auto_expandable : true,
                ea_placement_sharing: placement.sharing || false,
                ea_placement_display_video: placement.video_teaser || false,
                ext_inv_code: invCode,
                status: placement.status,
                include_inventory_company_id: placement.inventory_company_id,
                include_inventory_master_company_id: placement.inventory_master_company_id,
                exclude_inventory_company_id: placement.inventory_company_id,
                exclude_inventory_master_company_id: placement.inventory_master_company_id,
                ea_device: deviceType,
                pt1: placement.quantum_ssp_id ? placement.quantum_ssp_id : '',
                allow_ast: placement.allow_ast ? placement.allow_ast : ''
            };
            var apx_test = win.ean.utils.getParameterByName('ean-apx-test');
            if (apx_test && apx_test == 'true') {
                loadConfig.test = 1;
            }
            if (placement.template.fb_placement_id) {
                loadConfig.facebook_init = true;
            }
            win.ean.utils.extend(loadConfig, placement.template.placement_params);
            win.ean.utils.extend(loadConfig, EAdConfig.queryParams);
            try {
                if (placement.template.placement_params_publisher) {
                    for (var prop in placement.template.placement_params_publisher) {
                        loadConfig [prop] = win.encodeURIComponent(win.ean.utils.getValueFromPath(win, placement.template.placement_params_publisher[prop])).replace(/[!'()*]/g, escape);
                    }
                }
            } catch (e) {
                if (window.console) {
                    window.console.log("EANative ERROR: Error adding placement_params_publisher:", e);
                }
            }

            eaNative.defineSPPTag(placement.placementID, loadConfig);
        }

    };
    eaNative.parseOpenRTBNativeAd = function (data) {
        var openRTBAd = {
            type: "openRTB",
            ah: "",
            adnxsId: data.tagId,
            sid: "",
            pubid: "",
            country: "",
            cpid: "",
            cid: data.creativeId,
            csize: "1x1",
            auctionid: data.auctionId,
            clickTAG: "",
            clickTrackers: data['native'].clickTrackers,
            sspData: "",
            uid: data.uuid,
            uip: "",
            cpCode: "",
            dsp: "openRTB",
            dspData: data['native'],
            trackClicksInBackground: true,
            enableEATrack: false
        };
        eaNative.initNativeAd(openRTBAd);

    };
    eaNative.openRTBInit = function (data) {

        var nativeData = data.dspData;
        var image_url = "";
        if (nativeData.image && nativeData.image.url.length > 0) {
            image_url = nativeData.image.url;
        }

        var brand_name = "";
        var brand_url = "";

        if (nativeData.sponsoredBy) {
            if (nativeData.sponsoredBy.indexOf("http") !== -1) {
                brand_url = nativeData.sponsoredBy;
            } else {
                brand_url = nativeData.clickUrl;
                brand_name = nativeData.sponsoredBy;
            }
        } else {
            brand_url = nativeData.clickUrl;
        }
        var eanExternAdSettings = {
            settings: {
                ah: data.ah,
                sharer_domain: null,
                tags: {
                    autoexpand_content_type: "",
                    brand_logo: nativeData.icon.url,
                    brand_name: brand_name,
                    brand_url: brand_url,
                    content_link: nativeData.clickUrl,
                    content_type: "content_link",
                    expanded_content_type: "none",
                    expanded_summary: "",
                    image_landscape: image_url,
                    image_large_landscape: image_url,
                    image_portrait: image_url,
                    image_square: image_url,
                    overlay_content: "",
                    overlay_content_type: "embed",
                    summary: nativeData.body,
                    teaser_type: "",
                    title: nativeData.title,
                    video_url: ""
                },
                type: "native"
            }
        };
        eaNative.show(eanExternAdSettings);
        while (nativeData.impressionTrackers && nativeData.impressionTrackers.length > 0) {
            win.ean.utils.loadScript(nativeData.impressionTrackers.shift());
        }
    };
    eaNative.redirectToQuantX = function(onLoadCallback)
    {
        var queryStrings = "";
        if(eaNative.nativeEmbedScriptQuery) {
            queryStrings = '?' + eaNative.nativeEmbedScriptQuery;
        }

        var head = window.document.getElementsByTagName("head")[0];
        var scr = window.document.createElement("script");
        scr.type = "text/javascript";
        scr.onload = onLoadCallback;
        scr.setAttribute("id", 'quantx-embed-tag');
        scr.async = true;
        scr.src = eaNative.URLPaths.quantxNativeEmbedURL + queryStrings;
        head.appendChild(scr);
        return scr;
    },
    eaNative.initPlacement = function (data) {
        if (win.ean.utils.getParameterByName('ean-test-quantx') == "true") {
            win.ean.forceReloadScript = true;
            //eaNative.utils.loadScript(eaNative.URLPaths.quantxNativeEmbedURL, function () {},false, 'quantx-embed-tag');
            eaNative.redirectToQuantX();
            return false;
        } else {
            for (var i = 0; i < data.length; i++) {
                if (data[i].hasOwnProperty('exported_to_quantx') && data[i].exported_to_quantx == true) {
                    win.ean.forceReloadScript = true;
                    break;
                }
            }
        }

        if(win.ean.forceReloadScript && win.ean.utils.getParameterByName('ean-test-ttj') != "true"
                                            && win.ean.utils.getParameterByName('ean-test-ast') != "true") {
            //eaNative.utils.loadScript(eaNative.URLPaths.quantxNativeEmbedURL, function () {},false, 'quantx-embed-tag');
            eaNative.redirectToQuantX();
            return false;
        }

        for (var i = 0; i < data.length; i++) {
            var placement = data[i];

            if (placement.allow_ast==true) {
                EAdConfig.sspTAG = "ast";
            }

            if (win.ean.utils.getParameterByName('ean-test-ttj') == "true") {
                EAdConfig.sspTAG = "ttj";
            }

            eaNative.placementsById [placement.pid] = placement;
            switch (placement.type) {
                case "native":

                    if (placement && win.ean.utils.checkStatus(placement) &&
                            placement.template && placement.template.selector) {

                        // --> NAT 3533
                        if ((EAdConfig.queryParams["force-pid"] && placement.pid == EAdConfig.queryParams["force-pid"]) || !EAdConfig.queryParams["force-pid"]) {
                            // <-- NAT 3533

                            var selector = placement.template.selector;
                            var alternative_selectors = placement.template.alternative_selectors ? placement.template.alternative_selectors : null;
                            if (alternative_selectors && !win.ean.utils.isArray(alternative_selectors)) {
                                alternative_selectors = [alternative_selectors];
                            }
                            if (!win.ean.utils.isArray(selector)) {
                                selector = [selector];
                            }
                            var placementContainerFound = false;
                            var containerSelector = selector;
                            for (var s = 0; s < selector.length; s++) {
                                if (placement.template.check_selector_with_interval) {
                                    if (!selector[s].includes(":not(.ea-old)") > -1) {
                                        selector[s] += ":not(.ea-old)";
                                    }
                                }
                                containerSelector = selector[s];
                                try {
                                    var container = win.document.querySelector(containerSelector);
                                } catch (e) {
                                }
                                ;

                                if (!container && alternative_selectors) {
                                    var k;
                                    if (alternative_selectors[s]) {
                                        for (k = 0; k < alternative_selectors[s].length; k++) {
                                            containerSelector = alternative_selectors[s][k];
                                            try {
                                                container = win.document.querySelector(containerSelector);
                                            } catch (e) {
                                            }
                                            ;
                                            if (container) {
                                                placement.template.selector = containerSelector;
                                                break;

                                            }
                                        }
                                    }
                                }

                                if (container) {
                                    if (placement.template.fixedPosition == true) {
                                        if (container.children.length < parseInt(placement.template.position)) {
                                            placementContainerFound = false;
                                            break;
                                        } else {
                                            placementContainerFound = true;
                                            break;
                                        }
                                    } else {
                                        placementContainerFound = true;
                                    }

                                    break;
                                }

                            }
                            if (placementContainerFound) {
                                var placementID = placement.placementID;
                                if (win.ean.utils.getParameterByName("ean-test-native") || win.ean.utils.getParameterByName("ean-testall-native")) {
                                    placementID = EAdConfig.defaultPlacementId;
                                }
                                placement.placementID = placementID;

                                var method = placement.template.method || placement.method;
                                switch (method) {
                                    default :
                                        if (placement.sspParsed !== true) {
                                            if (win.ean.utils.getParameterByName('ean-placement-preview') == "true" || win.ean.utils.getParameterByName('ean-isinpreview') == "1" || String(placement.skip_visibility_test) == "true" || win.ean.utils.isElementVisible(containerSelector, method, placement)) {
                                                placement.sspParsed = true;
                                                eaNative.initSSPTag(placement);
                                                eaNative.removePlacementForScrollingChecking(placement);
                                            } else {
                                                eaNative.addPlacementForScrollingChecking(placement);
                                            }
                                        } else {
                                            eaNative.addPlacementForScrollingChecking(placement);
                                        }
                                        break;
                                    case "every":
                                        placement.template.position = parseInt(placement.template.position);
                                        if (isNaN(placement.template.position))
                                            placement.template.position = 0;
                                        var validChildrens = eaNative.utils.getValidChildren(placement.template.child_selector ? win.document.querySelectorAll(placement.template.child_selector) : container.children);

                                        var inventoryElements = [];

                                        for (var v = 0; v < validChildrens.length; v++) {
                                            var cClass = validChildrens[v].getAttribute("class");
                                            if (!cClass || cClass.indexOf("ean_element") == -1) {
                                                inventoryElements.push(validChildrens[v]);
                                            }
                                        }
                                        var position = placement.template.position;
                                        var frequency = placement.template.frequency || 2;
                                        var placementsCount = (inventoryElements.length - position) / (frequency) + 1;

                                        placementsCount = Math.max(Math.round(placementsCount), 0);
                                        if (!placement.sspParsed)
                                            placement.sspParsed = {};


                                        var placementToAddForChecking = false;
                                        while (placementsCount) {
                                            if (!placement.sspParsed[position]) {
                                                if (win.ean.utils.getParameterByName('ean-placement-preview') == "true" ||
                                                    win.ean.utils.getParameterByName('ean-isinpreview') == "1" ||
                                                    String(placement.skip_visibility_test) == "true" ||
                                                    win.ean.utils.isElementVisible(containerSelector, method, placement, position)) {

                                                    var placementData = JSON.parse(JSON.stringify(placement));
                                                    placementData.template.position = ((position < container.children.length - 1)
                                                    && inventoryElements[position]) ? win.ean.utils.getElementIndex(container.children, inventoryElements[position]) : "last";
                                                    placementData.template.method = "insert_at";
                                                    eaNative.initSSPTag(placementData);

                                                    placement.sspParsed[position] = true;
                                                } else {
                                                    placementToAddForChecking = true;
                                                }

                                            }
                                            placementsCount--;
                                            position += frequency;
                                        }
                                        if (placementToAddForChecking == false) {
                                            eaNative.removePlacementForScrollingChecking(placement);
                                        } else {
                                            eaNative.addPlacementForScrollingChecking(placement);
                                        }

                                        break;
                                }


                            } else {
                                if (!placement.parsed && placement.template.initDelay || placement.template.check_selector_with_interval) {
                                    placement.parsed = true;
                                    clearTimeout(placement.delayTimer);

                                    var callWithDelay = function (delayedPlacement) {
                                        delayedPlacement.delayTimer = setTimeout(function () {
                                            eaNative.initPlacement([delayedPlacement]);
                                        }, parseInt(delayedPlacement.template.initDelay || 1000));
                                    };
                                    callWithDelay(placement);
                                }

                            }
                        }
                    }
                    break;
                case "article":
                    if (placement && win.ean.utils.checkStatus(placement) && placement.article_template && placement.articlePID === win.ean.utils.getParameterByName("ean-article-pid")) {
                        var url = win.ean.utils.getParameterByName('adArticleJsonPath');
                        if (!url) {
                            url = URLPaths.articleURL.replace("{hash}", win.ean.utils.getParameterByName("ean-article-id"));
                        }
                        eaNative.loadAdSettings(url, placement.placementID);
                        eaNative.articlePlacement = placement;
                        return;//in case there are more placements, we take just first when we are on articles page.
                    }
                    break;
            }
            ;
        }

        //if (!EAdConfig.queryParams["force-pid"]) {
        eaNative.loadSSPSpots();
        if (!win.ean.retargetPixelLoaded) {
            for (var i = 0; i < data.length; i++) {
                if (data[i].allow_retarget_pixel == 'true') {
                    eaNative.callRetargetingPixels();
                    win.ean.retargetPixelLoaded = true;
                    break;
                }
            }
        }
        //}
        /*else {
            if (placement.pid != EAdConfig.queryParams["force-pid"]) {
                eaNative.loadSSPSpots();
                if (!win.ean.retargetPixelLoaded) {
                    for (var i = 0; i < data.length; i++) {
                        if (data[i].allow_retarget_pixel == 'true') {
                            eaNative.callRetargetingPixels();
                            win.ean.retargetPixelLoaded = true;
                            break;
                        }
                    }
                }
            }
        }*/
    };
    eaNative.trackClickTag = function (adSettings) {
        //console.log('trackClickTag',adSettings);
        var clickTags       = [],
            clickImgTags    = [];

        if (adSettings.click_trackers_images) {
            if (adSettings.click_trackers_images != undefined) {
                if (adSettings.click_trackers_images.length > 0) {
                    clickImgTags = clickImgTags.concat(adSettings.click_trackers_images);
                } else {
                    clickImgTags.push(adSettings.click_trackers_images);
                }
            }
        }

        if (adSettings.click_trackers) {
            if (adSettings.click_trackers != undefined) {
                if (adSettings.click_trackers.length > 0) {
                    clickTags = clickTags.concat(adSettings.click_trackers);
                } else {
                    clickTags.push(adSettings.click_trackers);
                }
            }
        }

        if (adSettings.adnx) {
            if (adSettings.adnx.clickTAG) {
                clickTags.push(adSettings.adnx.clickTAG)
            }
            if (adSettings.adnx.clickTrackers != undefined) {
                if (adSettings.adnx.clickTrackers.length > 0) {
                    clickTags = clickTags.concat(adSettings.adnx.clickTrackers);
                } else {
                    clickTags.push(adSettings.adnx.clickTrackers);
                }
            }
        }

        while (clickTags.length) {
            win.ean.utils.loadScript(clickTags.shift());
        }

        while (clickImgTags.length) {
            win.ean.utils.loadTrackerImage(clickImgTags.shift());
        }

    };
    eaNative.initNativeAd = function (data) {
        var clonedData = JSON.parse(JSON.stringify(data));
        if (!data.ah) {
            data.ah = "c_" + win.ean.utils.getRand();
        }
        eaNative.setUID(data.uid);
        var nativeAds = eaNative.nativeAds[data.ah] || [];
        nativeAds.push(data);
        eaNative.nativeAds[data.ah] = nativeAds;

        var placementData = eaNative.placements [data.adnxsId];
        if (placementData && placementData.length != undefined)
            placementData = placementData.shift();

        if (placementData) {
            //console.log('ean initNativeAd',data);
            eaNative.processedPlacements[data.adnxsId] = eaNative.processedPlacements[data.adnxsId] || [];
            eaNative.processedPlacements[data.adnxsId].push(placementData);

            if (data.type == "native") {
                var url = data.adTeaserJsonPath ? data.adTeaserJsonPath : URLPaths.nativeURL.replace("{hash}", data.ah);
                if (data.dsp && !eaNative.isIntegrationDSP[data.ah]) {
                    eaNative.loadAdSettings(url, placementData.pid);
                }

            } else {
                if (!eaNative[data.dsp + "Init"]) {
                    var ifrDoc = null;
                    if (win.ean.iframes[data.adnxsId] && (win.ean.iframes[data.adnxsId].contentWindow.APN_macros !== undefined)) {
                        var ifrWIN = win.ean.iframes[data.adnxsId].contentWindow;
                        ifrWIN.ean = ifrWIN.ean || {};
                        ifrWIN.ean.placementDefaultCreativeHandler = ifrWIN.ean.placementDefaultCreativeHandler || function (param) {
                            win.ean.placementDefaultCreativeHandler(param);
                        };
                        ifrWIN.ean.initNativeAd = ifrWIN.ean.initNativeAd || function (eanADNXData) {
                            win.ean.initNativeAd(eanADNXData);
                        };
                        var med_iFrames = win.ean.iframes[data.adnxsId].contentDocument.getElementsByTagName('iframe');
                        ifrDoc = med_iFrames[med_iFrames.length - 1];
                    }
                    win.ean.utils.loadScript(
                            URLPaths.integrationHelperURL.replace("{dsp}", data.dsp),
                            function () {
                                eaNative[data.dsp + "Init"](data);
                            },
                            ifrDoc
                            );
                } else {
                    eaNative[data.dsp + "Init"](data);
                }
            }
        } else {
            if (win.qne) {
                win.qne.initNativeAd(clonedData);
            }
        }

    };
    eaNative.showPrismFormat = function (data) {
        return win.ean.utils.isMobile() && data.tags && data.tags.mobile_content_type == "prism"
                && data.tags.mobile_content && data.tags.mobile_content != "";
    };
    eaNative.loadAdSettings = function (url, pid) {
        if (win.ean.utils.getParameterByName("ean-test-hash") && win.ean.utils.getParameterByName("ean-preview")) {
            url += "_temp";
        }
        var script = win.document.createElement("script");
        script.type = "text/javascript";
        url += (url.indexOf("?") != -1) ? "&" : "?";
        script.src = url + "r=" + win.ean.utils.getRand() + "&uid=" + EAdConfig.uid + "&vid=" + new Date().getTime() + "." + Math.random().toString(36).slice(-8) + "&pid=" + pid;
        ;
        win.document.getElementsByTagName("head")[0].appendChild(script);
    };
    eaNative.createVId = function () {
        return new Date().getTime() + "." + Math.random().toString(36).slice(-8);
    };
    eaNative.show = function (data) {
        var selector = "";
        var template;
        var templateData;
        var placementData;
        var adSettings;
        var clickTAG = "";
        var clonedData = JSON.parse(JSON.stringify(data));

        //NAT 3533 -->
        if (EAdConfig.queryParams["force-pid"]) {
            var placements = eaNative.nativeAds[data.settings.ah];
            if (placements && placements.length) {
                adSettings = placements.shift();
                placementData = eaNative.processedPlacements [adSettings.adnxsId];
                if (placementData && placementData.length != undefined) {
                    placementData = placementData.shift();
                    templateData = placementData.template;
                    if (placementData.pid == EAdConfig.queryParams["force-pid"]) {
                        if (adSettings.clickTAG == "") {
                            var title = data.settings.tags.title.length > 15 ? data.settings.tags.title.substr(0.15) : data.settings.tags.title;
                            var description = data.settings.tags.summary.length > 100 ? data.settings.tags.summary.substr(0,100) : data.settings.tags.summary;
                            var sponsor = data.settings.tags.brand_name.length > 15 ? data.settings.tags.brand_name.substr(0,15) : data.settings.tags.brand_name;
                            var out = title + "|" + description + "|" + adSettings.clickTrackers[0] + "|_blank|#f7ecec|" + sponsor + "|bottom:0;left:0|" + adSettings.dspData.image.url;
                        }
                        else {
                            if (adSettings.dsp == 'a') {
                                for (var i in data.settings.tags) {
                                    if (i == 'image_landscape') {
                                        var scaleMode = defaultImageScaleMode [i] || defaultImageScaleMode.defaultScale;
                                        if (templateData.images_scalemode && templateData.images_scalemode[i]) {
                                            scaleMode = templateData.images_scalemode[i];
                                        }
                                        var image_url = eaNative.getURLPath("resizeServerImagesPath", adSettings.dsp).replace("{hash}", data.settings.tags.image_landscape[0]).replace("{wxh}", templateData.images_size[i]).replace("{scalemode}", scaleMode);
                                    }
                                }
                            }
                            else {
                                var image_url = data.settings.tags.image_landscape;
                            }
                            if (data.settings.tags.content_link != "") {
                                var clickTagURL = adSettings.clickTAG + encodeURIComponent(data.settings.tags.content_link);
                            }
                            else {
                                var clickTagURL = adSettings.clickTAG + encodeURIComponent(data.settings.tags.brand_url);
                            }
                            var title = data.settings.tags.title.length > 15 ? data.settings.tags.title.substr(0.15) : data.settings.tags.title;
                            var description = data.settings.tags.summary.length > 100 ? data.settings.tags.summary.substr(0, 100) : data.settings.tags.summary;
                            var sponsor = data.settings.tags.brand_name.length > 15 ? data.settings.tags.brand_name.substr(0, 15) : data.settings.tags.brand_name;
                            var out = title + "|" + description + "|" + clickTagURL + "|_blank|#f7ecec|" + sponsor + "|bottom:0;left:0|" + image_url;
                        }
                        if (EAdConfig.queryParams["outputElementId"]) {
                            win.document.getElementById(EAdConfig.queryParams["outputElementId"].trim()).innerHTML = out;
                        } else {
                            console.log("Element " + EAdConfig.queryParams["outputElementId"].trim() + " does not exist.");
                        }
                        return;
                    }
                }
            }
        }
        //NAT 3533 <--

        if (win.ean.qneArticleSettings && win.ean.qneArticleSettings.length > 0) {
            for (var index in win.ean.qneArticleSettings) {
                if (win.ean.qneArticleSettings[index] == clonedData.settings.ah) {
                    delete win.ean.qneArticleSettings[index];
                    if (win.qne) {
                        win.qne.show(clonedData);
                        return;
                    }
                }
            }
        }

        if (data && data.settings) {
            eaNative.nativeAdsSource [data.settings.ah] = data.settings;

            switch (data.settings.type) {
                case "native":

                    var placements = eaNative.nativeAds[data.settings.ah];

                    if (!data.settings.vid) {
                        data.settings.vid = this.createVId();
                    }

                    if(!data.settings.expand_vid) {
                        data.settings.expand_vid = this.createVId();
                    }

                    if (placements && placements.length) {
                        adSettings = placements.shift();
                        data.settings.adnx = adSettings;
                        data.settings.adnxsId = adSettings.adnxsId;
                        placementData = eaNative.processedPlacements [adSettings.adnxsId];
                        if (placementData && placementData.length != undefined)
                            placementData = placementData.shift();
                        if (placementData) {
                            //adaugare pixel cand se incarca o campanie din apx
//                            if (placementData.allow_retarget_pixel == 'true') {
//                                var apxPixel = '//pixel.adsafeprotected.com/jload?anId=925893&advId=${ADV_ID}&campId=${CP_ID}&placementId=${CREATIVE_ID}&pubId=${PUBLISHER_ID}&chanId=${TAG_ID}&respID=${RESERVE_PRICE}&ias_adpath=%5Bclass~%3Dea_apx_pid_${TAG_ID}%5D';
//                                apxPixel = apxPixel.replace('${CP_ID}', data.settings.adnx.cpid);
//                                apxPixel = apxPixel.replace('${CREATIVE_ID}', data.settings.adnx.cid);
//                                apxPixel = apxPixel.replace('${PUBLISHER_ID}', data.settings.adnx.pubid);
//                                apxPixel = apxPixel.replace('${TAG_ID}', data.settings.adnxsId).replace('${TAG_ID}', data.settings.adnxsId);
//                                win.ean.utils.loadScript(win.location.protocol + apxPixel);
//                            }
                            templateData = placementData.template;
                            template = placementData.template.template_html;

                            data.settings.tags['data-vid'] = data.settings.vid;
                            data.settings.tags['data-expand-vid'] = data.settings.expand_vid;

                            if (adSettings.clickTAG) {
                                clickTAG = adSettings.clickTAG;
                            }

                            selector = placementData.template.selector;
                        } else {
                            if (win.qne) {
                                win.qne.show(clonedData);
                            }
                            return;
                        }
                    } else {
                        if (win.qne) {
                            win.qne.show(clonedData);
                        }
                        return;
                    }
                    break;
                case "article":
                    placementData = eaNative.articlePlacement;
                    data.settings.adnx = data.settings;
                    data.settings.feac = win.ean.utils.getParameterByName("ean-feac");
                    data.settings.sid = win.ean.utils.getParameterByName("ean-sid");
                    data.settings.pubid = win.ean.utils.getParameterByName("ean-pubid");
                    data.settings.country = win.ean.utils.getParameterByName("ean-country");
                    data.settings.cpid = win.ean.utils.getParameterByName("ean-cpid");
                    data.settings.ah = win.ean.utils.getParameterByName("ean-article-id");
                    data.settings.uid = win.ean.utils.getParameterByName("ean-uid");
                    data.settings.vid = win.ean.utils.getParameterByName("ean-vid");
                    data.settings.adnxsId = win.ean.utils.getParameterByName("ean-pid");
                    data.settings.dsp = win.ean.utils.getParameterByName("ean-dsp");

                    adSettings = data.settings;
                    templateData = placementData.article_template;
                    template = placementData.article_template.template_html;
                    selector = placementData.article_template.selector;
                    placementData.pid = win.ean.utils.getParameterByName("ean-pid");/*placementData.articlePID;*/

                    if (data.settings.tags.title)
                        win.document.title = data.settings.tags.title;
                    if (data.settings.tags.content_type == "overlay_content" || data.settings.tags.content_type == "internal_content_link")
                        data.settings.tags.full_text = eaNative.getHTMLTag(data.settings.tags, data.settings.tags.content_type);

                    win[add](pre + "blur", eaNative.stopUITTracking, false);
                    win[add](pre + "focus", function () {
                        eaNative.startUITTracking(data.settings.vid)
                    }, false);

                    eaNative.uit = 0;
                    eaNative.uitInterval = 1000;
                    eaNative.startUITTracking(data.settings.vid);
                    break;
            }

            eaNative.nativeAdsSourceByVid[data.settings.expand_vid]
                = eaNative.nativeAdsSourceByVid[data.settings.vid]
                = data.settings;

            eaNative.tagsIndexes = {};
            eaNative.viewsTrack = [];

            //console.log('placementData la show:',placementData);

            if (placementData) {

                data.settings.pid = placementData.pid;
                adSettings.placementConfig = placementData;
                var articleLink = placementData.link || "";
                var testHash = win.ean.utils.getParameterByName("ean-test-hash");
                if (eaNative.showPrismFormat(data.settings)) {
                    articleLink = URLPaths.prismContentURL + data.settings.tags.mobile_content;
                    data.settings.tags.content_type = "content_link";
                    if (!eaNative.prismCacheFlags)
                        eaNative.prismCacheFlags = {};
                    if (!eaNative.prismCacheFlags[data.settings.tags.mobile_content]) {
                        eaNative.prismCacheFlags[data.settings.tags.mobile_content] = true;
                        var preCacheContainer = doc.createElement("div");
                        preCacheContainer.style.display = "none";
                        doc.body.appendChild(preCacheContainer);
                        win.ean.utils.insertHTML(preCacheContainer, eaNative.getHTMLTag(data.settings.tags, "prism", ',"pre-cache":true'));
                        eaNative.parseScripts(preCacheContainer.children);

                    }
                } else if (data.settings.tags && data.settings.tags.content_type == "content_link")
                    articleLink = data.settings.tags.content_link;
                else {
                    articleLink += (articleLink.indexOf("?") == -1) ? "?" : "&";
                    articleLink += "ean-article-id={hash}&ean-article-pid={apid}&ean-uid={uid}&ean-vid={vid}&ean-pid={pid}&ean-sid={sid}&ean-cpid={cpid}&ean-pubid={pubid}&ean-country={ean-country}&ean-feac={feac}&ean-dsp={dsp}";
                    if (win.ean.utils.getParameterByName("ean-test-native")) {
                        articleLink += "&ean-test-native=true"
                    }
                    if (win.ean.utils.getParameterByName("ean-preview")) {
                        articleLink += "&ean-preview=true"
                    }
                    if (win.ean.utils.getParameterByName("ean-testall-native")) {
                        articleLink += "&ean-testall-native=true"
                    }
                    if (testHash) {
                        articleLink += "&ean-test-hash=" + win.ean.utils.getParameterByName("ean-test-hash");
                        isTestHash = true;
                    }
                    if (win.ean.utils.getParameterByName("ean-debug")) {
                        articleLink += "&ean-debug=true";
                    }
                    if (data.settings.adnx.dsp != "a") {
                        articleLink += "&adArticleJsonPath=" + encodeURIComponent(data.settings.adnx.adArticleJsonPath);
                    }

                    articleLink = win.ean.utils.replaceParam(articleLink, "hash", data.settings.ah);
                    articleLink = win.ean.utils.replaceParam(articleLink, "apid", placementData.articlePID);
                    articleLink = win.ean.utils.replaceParam(articleLink, "uid", EAdConfig.uid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "vid", data.settings.vid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "pid", placementData.pid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "sid", adSettings.sid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "cpid", adSettings.cpid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "pubid", adSettings.pubid);
                    articleLink = win.ean.utils.replaceParam(articleLink, "feac", adSettings.feac);
                    articleLink = win.ean.utils.replaceParam(articleLink, "ean-country", adSettings.country);
                    articleLink = win.ean.utils.replaceParam(articleLink, "dsp", adSettings.dsp);

                }
                if (!testHash) {
                    articleLink = eaNative.utils.replaceCreativeMacros(articleLink, adSettings);
                }

                data.settings.tags.encoded_article_link = encodeURIComponent(articleLink);
                data.settings.tags.encoded_title = encodeURIComponent(data.settings.tags.title);
                data.settings.tags.article_link = articleLink;
                data.settings.tags.ah = data.settings.ah;
                if (data.settings.tags.content_type == "content_link") {
                    data.settings.tags.share_page_url = data.settings.tags.content_link;
                } else {
                    data.settings.tags.share_page_url = encodeURIComponent(URLPaths.sharePageUrl
                            .replace("{ah}", data.settings.ah)
                            .replace("{encoded_article_link}", data.settings.tags.encoded_article_link)
                            .replace("{share_page_domain_url}",
                                    win.ean.utils.replaceDiacritics(String(data.settings.tags.brand_name)).replace(/[^\w\s]/gi, '').replace(/ /g, "-").toLowerCase() +
                                    "." + (data.settings.sharer_domain || URLPaths.defaultSharePageUrl)));
                }

                if (clickTAG) {
                    articleLink = adSettings.clickTAG + encodeURIComponent(articleLink);
                    data.settings.tags.brand_url = adSettings.clickTAG
                            + encodeURIComponent(data.settings.tags.brand_url);
                }
                data.settings.tags.link = articleLink;

                var method = templateData.method || placementData.method;

                if (!win.ean.utils.isArray(selector)) {
                    selector = [selector];
                }
                if (!win.ean.utils.isArray(template)) {
                    template = [template];
                }

                if (!win.ean.utils.isArray(method)) {
                    method = [method];
                }

                var toString = Object.prototype.toString;
                for (var i in data.settings.tags) {
                    var tagValue = data.settings.tags[i];

                    if (toString.call(tagValue) === "[object String]" && i !== "article_link") {
                        tagValue = eaNative.utils.replaceCreativeMacros(tagValue, adSettings);
                    }
                    if (win.ean.utils.isArray(tagValue)) {
                        var validValues = [];
                        for (var v = 0; v < tagValue.length; v++) {
                            if (tagValue[v] && tagValue[v] != null && tagValue[v] !== "null") {
                                validValues.push({value: tagValue[v], position: v});
                            }
                        }
                        var tagIndex = Math.floor(Math.random() * validValues.length * 0.999999999);
                        tagValue = validValues[tagIndex].value;
                        if (template.join().indexOf("{" + i + "}") != -1) {
                            eaNative.tagsIndexes[i] = tagValue;
                            var track = {ih: tagValue};
                            switch (i) {
                                default:
                                    track.eis = 102;
                                    break;
                            }
                            eaNative.viewsTrack.push(track);
                        }
                    }

                    if (templateData.images_size && tagValue !== "") {
                        if (templateData.images_size[i]) {

                            var hash = tagValue;
                            if (data.settings.adnx.dsp != "a") {
                                var encodedTagValue = B64.encode(tagValue);
                                hash = win.ean.utils.base64url_encode(encodedTagValue);

                                data.settings.tags["original_" + i] = tagValue;
                            } else {
                                data.settings.tags["original_" + i] = URLPaths.originalImagesPath.replace("{hash}", tagValue);
                            }
                            data.settings.tags["hash_" + i] = hash;

                            var scaleMode = defaultImageScaleMode [i] || defaultImageScaleMode.defaultScale;
                            if (data.settings.adnx.dsp == "antvoice" || data.settings.adnx.dsp == "kelkoo") {
                                scaleMode = 'scaleboxv2';
                            } else if (data.settings.adnx.dsp == "criteo") {
                                scaleMode = 'scalebox';
                            } else if(data.settings.quantx && data.settings.quantx.criteoAd) {
                                if(data.settings.quantx.criteoAd.fillimages === 0) {
                                    scaleMode = 'scalebox';
                                }
                            }
                            else if (templateData.images_scalemode && templateData.images_scalemode[i]) {
                                scaleMode = templateData.images_scalemode[i];
                            }
                            tagValue = eaNative.getURLPath("imagesPath", data.settings.adnx.dsp).replace("{hash}", hash).replace("{wxh}", templateData.images_size[i]).replace("{scalemode}", scaleMode);
                        }
                    }
                    data.settings.tags[i] = tagValue;
                }


                for (var i = 0; i < selector.length; i++) {
                    win.ean.utils.replaceHtml(template[i], data.settings, selector[i], method[Math.min(i, method.length - 1)], placementData, templateData);
                }
                data.settings.performance = eaNative.tagsIndexes;

            }
            this.trackEvent({v: [{es: trackingEvents.general}], ev: eaNative.viewsTrack}, data.settings, false);
            //add events

            if (data.settings.type == "native") {
                var self = this;

                data.settings.tags.expanded_summary = win.ean.utils.replaceParam(data.settings.tags.expanded_summary, "CACHEBUSTER", Math.floor(Math.random() * 100000));
                if (data.settings.adnx) {
                    data.settings.tags.expanded_summary = win.ean.utils.replaceParam(data.settings.tags.expanded_summary, "CLICK_URL_ENC", encodeURIComponent(data.settings.adnx.clickTAG));
                }
                if (win.ean.utils.isMobile()) {
                    if (data.settings.tags.mobile_content_type == "prism") {
                        data.settings.tags.content_type = "prism"
                    }
                }

                var summaryTags = win.document.querySelectorAll(".ea_summary,.ea_expanded_summary");
                for (var i = 0; i < summaryTags.length; i++) {
                    var element = summaryTags[i];
                    if (!element.getAttribute("data-ea-parsed")) {
                        element.setAttribute("data-ea-parsed", "true");
                        element.setAttribute("data-vid", data.settings.expand_vid);
                    }
                }

                var expandLinksNoHide = win.document.querySelectorAll(".ea_expand,.ea_expand_hide");

                if (String(data.settings.tags.expanded_content_type) !== 'none' && String(placementData.expandable) === 'true' && String(placementData.accept_auto_expandable) != 'false' && String(data.settings.tags.autoexpand_content_type) === 'true') {
                    data.settings.expand_status = true;
                    eaNative.clickExpandHide(data.settings);

                }
                for (var i = 0; i < expandLinksNoHide.length; i++) {
                    var element = expandLinksNoHide[i];
                    if (!element.getAttribute("data-ea-parsed")) {

                        element.setAttribute("data-ah", data.settings.ah);
                        element.setAttribute("data-vid", data.settings.vid);
                        element.setAttribute("data-pid", placementData.pid);
                        element.setAttribute("data-ea-content", data.settings.tags.content_type);
                        if (!data.settings.tags.expanded_summary || String(data.settings.tags.expanded_content_type) === 'none') {
                            element.setAttribute("class", element.getAttribute("class") + " ea_link");
                            continue;
                        }
                        element.setAttribute("data-ea-parsed", "true");

                        var expandHandler = function (ev) {
                            if (!ev)
                                ev = window.event;

                            var vid = this.getAttribute("data-vid");
                            var adSettings = eaNative.nativeAdsSourceByVid[vid];
                            if (adSettings.expand_status != true) {
                                eaNative.clickExpandHide(adSettings);
                                adSettings.expand_status = true;
                                self.trackEvent({ev: [{a: trackingEvents.click, es: trackingEvents.general}]}, adSettings);
                                eaNative.trackClickTag(adSettings);
                            } else {
                                if (ev.type == "click" || (ev.type == "touchend" && win.ean.utils.isMobile())) {
                                    if (this.getAttribute("href") == adSettings.tags.link) {
                                        if (win.location.hostname != win.ean.utils.getURLHostname(adSettings.tags.article_link)) {
                                            this.setAttribute("target", "_blank");
                                        }
                                    }
                                    eaNative.processLinkClickAction(this);
                                }
                            }

                            if (ev) {

                                if (ev.preventDefault) {
                                    ev.preventDefault();
                                } else {
                                    ev.returnValue = false;

                                }
                            }
                            return false;
                        };
                        element.onclick = element.ontouchend = expandHandler;
                        if (placementData.FLAG_MOBILE_CLICK_BUGFIX && win.ean.utils.isMobile()) {
                            element.ontouchend = null;
                        }
                        if (!win.ean.utils.isMobile() && data.settings.tags.autoexpand_content_type == "hover" &&
                                String(placementData.accept_auto_expandable) != 'false') {
                            element.onmouseover = expandHandler;
                        }
                    }
                }
                var expandLinksClose = win.document.querySelectorAll(".ea_expand_close");
                for (var i = 0; i < expandLinksClose.length; i++) {
                    var element = expandLinksClose[i];
                    if (!element.getAttribute("data-ea-parsed")) {

                        element.setAttribute("data-ah", data.settings.ah);
                        element.setAttribute("data-vid", data.settings.vid);
                        element.setAttribute("data-pid", placementData.pid);

                        element.setAttribute("data-ea-parsed", "true");
                        element.onclick = element.ontouchend = function (ev) {
                            if (!ev)
                                ev = window.event;

                            var ah = this.getAttribute("data-ah");
                            var vid = this.getAttribute("data-vid");
                            var pid = this.getAttribute("data-pid");
                            var adSettings = eaNative.nativeAdsSourceByVid[vid];
                            var placementData = eaNative.placementsById[pid];

                            var summaryContainer = win.document.querySelector(".ea_summary[data-vid='" + adSettings.vid + "']");
                            if (summaryContainer) {
                                var maxChar = null;
                                if (placementData && placementData.template && placementData.template.max_chars && placementData.template.max_chars["summary"]) {
                                    maxChar = placementData.template.max_chars["summary"];
                                }

                                win.ean.utils.insertHTML(summaryContainer, win.ean.utils.strip(adSettings.tags.summary, maxChar));
                            }


                            var expandLink = win.document.querySelector(".ea_expand_hide[data-vid='" + adSettings.vid + "']");
                            if (expandLink) {
                                expandLink.style.display = "";
                            }

                            this.style.display = "none";

                            var expandedSummaryContainer = win.document.querySelector(".ea_expanded_summary[data-vid='" + adSettings.vid + "']");
                            if (expandedSummaryContainer) {
                                win.ean.utils.insertHTML(expandedSummaryContainer, "");
                                expandedSummaryContainer.style.display = "none";
                            }
                            adSettings.expand_status = false;

                            if (ev && ev.preventDefault) {
                                ev.preventDefault();
                            } else {
                                ev.returnValue = false;
                            }
                            return false;
                        };
                        if (placementData.FLAG_MOBILE_CLICK_BUGFIX && win.ean.utils.isMobile()) {
                            element.ontouchend = null;
                        }
                    }
                }
                var links = win.document.querySelectorAll(".ea_link");
                for (var i = 0; i < links.length; i++) {
                    var element = links[i];
                    if (!element.getAttribute("data-ea-parsed")) {
                        element.setAttribute("data-ea-parsed", "true");
                        element.setAttribute("data-ea-content", data.settings.tags.content_type);
                        element.setAttribute("data-ah", data.settings.ah);
                        element.setAttribute("data-vid", data.settings.vid);
                        element.setAttribute("data-pid", placementData.pid);
                        if (data.settings.adnx && data.settings.adnx.dsp == "twenga") {
                            element.setAttribute("rel", "nofollow");
                        }

                        if (data.settings.tags.content_type == "content_link") {
                            element.setAttribute("target", "_blank");
                        }
                        var mainEvent = "onclick";
                        if (element.getAttribute("href") == data.settings.tags.link) {
                            if (win.location.hostname != win.ean.utils.getURLHostname(data.settings.tags.article_link)) {
                                element.setAttribute("target", "_blank");
                            }
                        }
                        if (data.settings.tags.content_type == "overlay_content" && element.getAttribute("href") != data.settings.tags.brand_url) {
                            element.setAttribute("oncontextmenu", "return false");
                            element.oncontextmenu = function (ev) {
                                if (ev && ev.preventDefault) {
                                    ev.preventDefault();
                                }
                                return false;
                            }
                            mainEvent = "onmousedown";
                        }

                        element[mainEvent] = element.ontouchend = function (ev) {
                            if (!ev)
                                ev = window.event;

                            eaNative.processLinkClickAction(this);
                            if (ev && ev.preventDefault) {
                                ev.preventDefault();
                                ev.stopPropagation();
                            } else {
                                ev.returnValue = false;
                            }
                            return false;
                        };
                        if (placementData.FLAG_MOBILE_CLICK_BUGFIX && win.ean.utils.isMobile()) {
                            element.ontouchend = null;
                        }
                    }
                }
                eaNative.measureAndSendSizeEvent(adSettings);
                //click fraud
                if (data.settings.cache_lp) {
                    if (Math.random() < data.settings.cache_lp) {
                        win.setTimeout(function () {
                            eaNative.loadLandingPageInIFrame(data.settings.tags.link);
                        }, 1000);

                    }
                }
                if (placementData.template.check_selector_with_interval) {
                    win.setTimeout(function() {
                        var layerDiv = document.getElementsByClassName("ead_" + placementData.pid);
                        for (var j = 0; j < layerDiv.length; j++) {
                            var parentLayerDiv = layerDiv[j].parentNode;
                            if ( typeof parentLayerDiv !== 'undefined' ) {
                                if (!parentLayerDiv.classList.contains("ea-old")) {
                                    parentLayerDiv.className = parentLayerDiv.className + " ea-old";
                                }
                            }
                        }

                        if (!(typeof win.ean === "undefined") && !(typeof placementData.pid === "undefined")) {
                            placementData.template.ea_native_element_added      = false;
                            win.ean.placementsById[placementData.pid].sspParsed = false;
                            win.ean.initPlacement([win.ean.placementsById[placementData.pid]]);
                        }
                    }, 1000);
                }
            }
            var shares = win.document.querySelectorAll(".ea_share");
            for (var i = 0; i < shares.length; i++) {
                var element = shares[i];
                if (!element.getAttribute("data-ea-parsed")) {
                    element.setAttribute("data-ea-parsed", "true");
                    element.setAttribute("data-ah", data.settings.ah);
                    element.setAttribute("data-vid", data.settings.vid);
                    element.setAttribute("data-pid", placementData.pid);

                    element.onclick = element.ontouchend = function (ev) {
                        if (!ev)
                            ev = window.event;

                        var ah = this.getAttribute("data-ah");
                        var vid = this.getAttribute("data-vid");
                        var pid = this.getAttribute("data-pid");
                        var adSettings = eaNative.nativeAdsSourceByVid[vid];

                        eaNative.trackEvent({ev: [{a: trackingEvents.click, es: trackingEvents.general}]}, adSettings);
                        eaNative.trackClickTag(adSettings);
                        this.setAttribute("target", "_blank");

                        switch (this.getAttribute("data-eashare")) {
                            default :
                                return;
                                //openURL(this.href,"_blank");
                                break;
                            case "facebook":
                            case "gplus":
                                win.ean.utils.openURL(this.href, "_blank", null, 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600');
                                break;
                        }
                        if (ev && ev.preventDefault) {
                            ev.preventDefault();
                        } else {
                            ev.returnValue = false;
                        }
                        return false;
                    };
                    if (placementData.FLAG_MOBILE_CLICK_BUGFIX && win.ean.utils.isMobile()) {
                        element.ontouchend = null;
                    }
                }
            }
            for (var tagName in data.settings.tags) {
                if (!data.settings.tags[tagName] || (data.settings.tags["hash_" + tagName] != undefined && data.settings.tags["hash_" + tagName] == "")) {
                    var tagElements = win.document.querySelectorAll(".ea_hide_" + tagName);
                    for (var i = 0; i < tagElements.length; i++) {
                        var element = tagElements[i];
                        if (element && !element.getAttribute("data-ea-hide-parsed")) {
                            element.setAttribute("data-ea-hide-parsed", "true");

                            element.style.display = "none";
                        }
                    }
                }
            }
            var images = win.document.querySelectorAll(".ea_image");
            for (var i = 0; i < images.length; i++) {
                var img = images[i];
                if (!img.getAttribute("data-ea-parsed")) {
                    if (placementData.video_teaser && data.settings.tags['video_url']) {
                        if (img.className.indexOf('ea_image_video') > -1) {
                            img.parentNode.removeChild(img);
                            continue;
                        }
                    }
                    img.setAttribute("data-ea-parsed", "true");
                    img.setAttribute("data-ea-dsp", data.settings.adnx.dsp);
                    if (!img.complete) {
                        img.onerror = function () {
                            var src = this.getAttribute("src");
                            var hashStart = src.indexOf("image/") + 6;
                            var hashEnd = src.indexOf("/", hashStart);
                            var hash = src.substring(hashStart, hashEnd);
                            var dsp = this.getAttribute('data-ea-dsp');

                            var sizeArr = src.split("-");
                            var size = sizeArr[sizeArr.length - 1];
                            var scaleMode = sizeArr [0].split("/");
                            scaleMode = scaleMode[scaleMode.length - 1];

                            if (hash && size && scaleMode) {
                                this.setAttribute("src", eaNative.getURLPath("resizeServerImagesPath", dsp).replace("{hash}", hash).replace("{wxh}", size).replace("{scalemode}", scaleMode));
                            }
                            this.onerror = null;
                        };
                    }
                }
            }

            var videos = win.document.querySelectorAll(".ea_video_vast");
            videos.length > 0 && eaNative.startPostMessageTracking(data.settings);
            for (var i = 0; i < videos.length; i++) {
                var video = videos[i];
                if (!video.getAttribute("data-ea-parsed")) {
                    if (!(placementData.video_teaser && data.settings.tags['video_url'])) {
                        video.parentNode.removeChild(video);
                        continue;
                    }

                    video.setAttribute('data-ea-parsed', 'true');
                    video.setAttribute("data-ah", data.settings.ah);
                    video.setAttribute("data-vid", data.settings.vid);
                    video.setAttribute("data-pid", placementData.pid);

                    video.appendChild(
                        eaNative.getHTMLTag(
                            data.settings.tags,
                            "video_url",
                            data.settings.vid,
                            placementData.template.expand_video_teaser_on_click,
                            placementData.template.expand_video_teaser_on_hover,
                            placementData.template.expand_video_teaser_text || "En savoir plus"
                        )
                    );

                    if(EAdConfig.debug === true) {
                        win.console.log(
                            'Video ' + data.settings.vid +
                            ' has pause option: ' + eaNative.teaserHasPauseOption(data.settings)
                        );
                    }

                    if(eaNative.teaserHasPauseOption(data.settings)) {
                        eaNative.addVastVideosForScrollingChecking(video);
                    }
                }
            }

            var adsElement = win.document.querySelectorAll(".ea_native_element");
            if(adsElement && data.settings.adnx.dsp === "yahoo") {
                for(var i = 0; i < adsElement.length; i++) {
                    eaNative.addAdElForScrollingChecking(adsElement[i], data);
                }
            }

            var ad_variables = data.settings.dynamic_variables;
            if (ad_variables) {
                for (var i = 0; i < ad_variables.length; i++) {
                    var dynamic_vars = win.document.querySelectorAll(".ea_creative_var_" + ad_variables[i].dynamic_variable);
                    for (var j = 0; j < dynamic_vars.length; j++) {
                        var dynamicEl = dynamic_vars[j];
                        if (!dynamicEl.getAttribute("data-ea-parsed")) {
                            dynamicEl.innerHTML = ad_variables[i].dynamic_value;
                            dynamicEl.setAttribute('data-ea-parsed', 'true');
                        }
                    }
                }
            }

            var scripts = win.document.querySelectorAll("[data-eascript=true]");
            eaNative.parseScripts(scripts);

            if (data.settings.type == "article") {
                var useBackground = data.settings.tags.use_skin_background;
                if (String(useBackground) === "true") {
                    var headImage = data.settings.tags.hash_skin_head_image;
                    var leftImage = data.settings.tags.hash_skin_left_image;
                    var rightImage = data.settings.tags.hash_skin_right_image;

                    var isImageAvailable = function (image) {
                        if (image && image.length) {
                            return true;
                        }
                        return false;
                    };
                    if (isImageAvailable(headImage) || isImageAvailable(leftImage) || isImageAvailable(rightImage)) {
                        var skinContainer = win.document.querySelector(".ea_background_skin");
                        if (skinContainer) {
                            skinContainer.style.display = "block";

                            var headImage = data.settings.tags.original_skin_head_image;
                            var img = new Image();
                            img.onload = function () {
                                var headContainer = win.document.querySelector(".ea_background_skin .ea_skin_head");

                                if (headContainer)
                                    headContainer.style.height = img.height + "px";
                            }
                            img.src = headImage;
                        }
                    }
                    win.document.body.style.background = data.settings.tags.skin_bgcolor;
                }
            }
        }
    };
    eaNative.teaserHasPauseOption = function (adSettingObject) {
        return Boolean(
            adSettingObject &&
            (
                !adSettingObject.hasOwnProperty('tags') ||
                !adSettingObject.tags.hasOwnProperty('vast_teaser_pause_option') ||
                adSettingObject.tags.vast_teaser_pause_option === "1"
            )
        )
    },
    eaNative.trackClickAction = function (element) {
        var href = element.getAttribute("href");
        var vid = element.getAttribute("data-vid");
        var pid = element.getAttribute("data-pid");
        var adSettings = eaNative.nativeAdsSourceByVid[vid];
        eaNative.trackEvent({ev: [{a: trackingEvents.click, es: trackingEvents.general}]}, adSettings);
    };
    eaNative.processLinkClickAction = function (element) {

        var ah = element.getAttribute("data-ah");
        var pid = element.getAttribute("data-pid");
        var vid = element.getAttribute("data-vid");
        var adSettings = eaNative.nativeAdsSourceByVid[vid];
        var contentType = element.getAttribute("data-ea-content");
        var href = element.getAttribute("href");
        var target = element.getAttribute("target");
        var emTarget = target;

        eaNative.trackClickAction(element);

        if (href == adSettings.tags.brand_url) {
            contentType = "";
            target = "_blank";
        } else if (eaNative.showPrismFormat(adSettings)) {
            contentType = "prism";
        }

        if (contentType == "content_link") {
            target = "_blank";
        }
        if (element.getAttribute("data-ea-target-set-by-placement") == "true") {
            target = emTarget;
        }
        //console.log('aiaiaia', contentType, adSettings);
        switch (contentType) {
            default :

                if (adSettings.click_trackers || adSettings.click_trackers_images) {
                    eaNative.trackClickTag(adSettings);
                }

                if (adSettings.adnx && adSettings.adnx.trackClicksInBackground == true) {
                    eaNative.trackClickTag(adSettings);
                }
                if (target == "_blank") {
                    win.ean.utils.openURL(href, target);
                } else {
                    setTimeout(function () {
                        win.ean.utils.openURL(href, target);
                    }, 500);
                }
                break;
            case "prism":
            case "overlay_content":
                var overlayContainer = eaNative.getOverlayContainer(vid, contentType);
                win.ean.utils.insertHTML(overlayContainer, eaNative.getHTMLTag(adSettings.tags, contentType));


                eaNative.trackEvent({v: [{es: trackingEvents.general}]}, adSettings);
                eaNative.trackClickTag(adSettings);

                eaNative.parseScripts(overlayContainer.children);

                if (win.document.body && win.document.body.parentElement) {
                    win.document.body.style.overflow = win.document.body.parentElement.style.overflow = "hidden";
                }
                if (adSettings.adnx.isInPreview) {
                    eaNative.sendSizeEvent(doc.body.offsetWidth, Math.max(400, doc.body.offsetHeight));
                }
                win.history.pushState({overlay: true}, null, null);
                var onpopstate = function (ev) {
                    if (ev) {
                        var stateData = ev.state;
                        win.removeEventListener && win.removeEventListener('popstate', onpopstate);
                        eaNative.closeOverlayContent();
                    }

                };

                win.addEventListener && win.addEventListener('popstate', onpopstate);
                break;

        }
    }
    eaNative.placementDefaultCreativeHandler = function (settings) {
        var placementData = eaNative.placementsByAPXID [settings.id];

        if (placementData) {
            try {
                var default_creative = JSON.parse(placementData.default_creative);
            } catch (e) {
                return;
            }
            if (placementData && placementData.default_creative && default_creative) {
                var selector = placementData.template.selector;
                if (win.ean.utils.isArray(selector)) {
                    selector = selector[0];
                }
                var method = placementData.template.method || placementData.method;
                if (win.ean.utils.isArray(method)) {
                    method = method[0];
                }
                var htmlSettings = {};
                htmlSettings.tags = {};


                var content = default_creative.code;
                if (default_creative.type == "raw-js") {
                    var scr = doc.createElement('script');
                    scr.setAttribute('type', 'text/javascript');
                    scr.text = content;
                    var head = doc.getElementsByTagName('head')[0];
                    if (head) {
                        head.appendChild(scr);
                    }
                } else {
                    content = win.ean.utils.parseContent(content);
                    win.ean.utils.replaceHtml(content, htmlSettings, selector, method, placementData, placementData.template);

                    var scripts = win.document.querySelectorAll("[data-eascript=true]");
                    eaNative.parseScripts(scripts);
                }
            }
        }
    }
    eaNative.getHTMLTag = function (tags, tagName, other, eventOnClick, eventOnHover) {
        other = other === undefined ? "" : other;
        var contentType = "";
        var style = "";
        var path = "vastEmbed";

        var vastMinHeight = "min-height: 315px;";
        switch (tagName) {
            case "prism":
                contentType = tagName;
                tagName = "mobile_content";
                break;
            case "expanded_summary":
                if (tags.expanded_content_type == "vast") {
                    contentType = "vast";
                    style = "max-height:315px";
                }
                break;
            case "internal_content_link":
                contentType = "external_url";
                if (tags.internal_content_link_height != "auto") {
                    style = "height:" + tags.internal_content_link_height + "px";
                }
                break;
            case "overlay_content":
                if (tags.overlay_content_type == "vast") {
                    contentType = "vast";
                    style = "max-width:640px;max-height:380px";
                }
                break;
            case "video_url":
                contentType = "vast";
                style = "display:block;";
                vastMinHeight = "";
                path = "vastTeaser";
                other = other !== "" ? '&vid=' + other : '';
                break;
        }
        var tagValue = tags[tagName];
        tagValue = tagValue.replace(/\{UNIQUE_ID\}/g, "id-" + win.ean.utils.getRand());

        switch (contentType) {
            case "external_url":
                var iOS = eaNative.utils.isIOS();
                var scrolling = (iOS || tags.internal_content_link_height == "auto") ? "no" : "auto";
                var html_code = '<iframe id="ean_external_embed" frameborder="0" width ="100%" height="315px" style="width:1px; min-width:100%;min-height: 315px;' + style + '" allowfullscreen="true" mozallowfullscreen="true" scrolling="' + scrolling + '" src="' + tags.internal_content_link + '"></iframe>';
                if (tags.internal_content_link_height == "auto") {
                    html_code += '<scr' + 'ipt>function listener(event){ if ( String(event.data).indexOf("ean_internal_height_") ==-1)return; var newHeight = String(event.data).substr(20) ;var iFrameContainer = document.getElementById("ean_external_embed"); if(Math.abs(iFrameContainer.offsetHeight-newHeight)<10)return ; iFrameContainer.style.height = newHeight+ "px";} if (window.addEventListener){ addEventListener("message", listener, false)} else {attachEvent("onmessage", listener)} </scr' + 'ipt>';
                }
                return html_code;
                break;
            case "vast":
                if (tags.vast_pause_option == 1 && path === "vastEmbed") {
                    other = '&vpo=1&vid=' + other;
                }
                if(eventOnClick) {
                    other += '&expand_video_teaser_on_click=' + eventOnClick;
                }
                if(eventOnHover) {
                    other += '&expand_video_teaser_on_hover=' + eventOnHover;
                }

                ///var embedUrl    = URLPaths[path] + other;

                if(path === 'vastTeaser') {
                    var embedUrl    = URLPaths[path] + encodeURIComponent(tagValue) + other;
                    var vastContent = 'ea_vast_content|' + tags.video_url;
                    var frmEm       = doc.createElement("iframe");

                    frmEm.src       = embedUrl;
                    frmEm.onload    = function () {
                        var frmCnt = (frmEm.contentWindow) ? frmEm.contentWindow : (frmEm.contentDocument.document) ?
                            frmEm.contentDocument.document : frmEm.contentDocument;

                        frmCnt.postMessage(vastContent, '*');
                        frmEm.onload = null;
                    };

                    frmEm.setAttribute('frameborder', 0);
                    frmEm.setAttribute('width', '100%');
                    frmEm.setAttribute('height', '100%');
                    frmEm.setAttribute('allowfullscreen', 'true');
                    frmEm.setAttribute('mozallowfullscreen', 'true');

                    return frmEm;
                }

                return '<iframe frameborder="0" width ="100%" height="100%" style="' + vastMinHeight + style + '" allowfullscreen="true" mozallowfullscreen="true" src="' + URLPaths[path] + encodeURIComponent(tagValue) + other + '"></iframe>';
                break;
            case "prism":
                var prismUniqueId = win.ean.utils.getRand();
                return '<div class="frame-container" style = "display:none" id="' + prismUniqueId + '"></div><script type="text/javascript"> var viewHash = "uid_" + new Date().getTime() + "_"' +
                        ' +Math.random().toString(16).substr(2); var containerId = "frame-container-"+viewHash; var' +
                        ' adData = { "hash":"' + tags.mobile_content + '", "size":"100%x50", "targetId":"' + prismUniqueId + '",' +
                        ' "clickTAG":"", "impression_urls":[], "content_impression_urls":[], "viewHash":viewHash, ' +
                        '"inline":' + (other == undefined) + ',"teaser":false' + other + ' }; ' +
                        'window.cajs = window.cajs ||{}; cajs.que = cajs.que||[]; cajs.que.push(function () {' +
                        ' cajs.showAd(adData); }); (function() { var cajsEl = document.createElement("script");' +
                        ' cajsEl.type = "text/javascript"; cajsEl.async = true; cajsEl.id = "contad-embed-tag";' +
                        ' cajsEl.src = "' + URLPaths.prismCanvasJS + '"; var headEl =' +
                        ' document.getElementsByTagName("head")[0];' +
                        ' headEl.insertBefore(cajsEl, headEl.firstChild); })(); </script>';
                break;
            default :
                return tagValue;
                break;
        }
    };
    eaNative.parseScripts = function (scripts) {
        var scriptsToProcess = [];
        for (var i = 0, scriptsLength = scripts.length; i < scriptsLength; i++) {
            var oldScript = scripts[i];
            if (oldScript && String(oldScript.tagName).toUpperCase() == "SCRIPT" && !oldScript.getAttribute("data-eaparsed")) {
                scriptsToProcess.push(oldScript);
            }
        }
        for (var i = 0, scriptsLength = scriptsToProcess.length; i < scriptsLength; i++) {
            var oldScript = scriptsToProcess[i];
            if (oldScript && String(oldScript.tagName).toUpperCase() == "SCRIPT" && !oldScript.getAttribute("data-eaparsed")) {
                var newScript = win.document.createElement("script");
                newScript.type = "text/javascript";
                oldScript.parentElement.appendChild(newScript);
                oldScript.parentElement.removeChild(oldScript);
                var attributes = oldScript.attributes;
                for (var a = 0; a < attributes.length; a++) {
                    newScript.setAttribute(attributes[a].name, attributes[a].value);
                }

                newScript.setAttribute("data-eaparsed", true);
                if (oldScript.src)
                    newScript.src = oldScript.src;
                else {
                    newScript.text = oldScript.innerHTML;
                }

            }
        }
    }
    eaNative.closeOverlayContent = function () {
        var container = win.document.getElementById("ea-overlay-content");
        if (container) {
            container.style.display = "none";
        }
        if (win.document.body && win.document.body.parentElement)
            win.document.body.style.overflow = win.document.body.parentElement.style.overflow = "";
        if (container && container.parentNode) {
            container.parentNode.removeChild(container);
        }
        var vid = container.getAttribute('data-vid');
        var adSettings = eaNative.nativeAdsSourceByVid[vid];
        eaNative.measureAndSendSizeEvent(adSettings.adnx);

    }
    eaNative.getOverlayCloseButton = function (contentType) {
        var closeButton = win.document.createElement("div");
        switch (contentType) {
            case "prism":
                closeButton.innerHTML = "<div style =\"background:" +
                        " url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAYCAYAAADKx8xXAAAAFHRFWHRTb2Z0d2FyZQBnbHVlLTAuMTEuMQ2IyEMAAAASdEVYdENvbW1lbnQANmUzZTQwZDliMpCixHYAAAB1SURBVHjaY2AgDXAwkAE4qqrrWlvbOgtI1vT9x4+fV69dv8nAwKBAkqbnz1+8TEvPtBnVNHw0MSFznjx99mLWzOlfyEpSp8+cu8jAwGAwqnl4az546MhJBgYGBRYiNf5oa21qZmBgYODm5n7NwMDwgIHc4hEAyhvOeKqBfrQAAAAASUVORK5CYII='); width: 14px; height: 24px; \"></div>"
                closeButton.setAttribute("style", "position:absolute; z-index:3147483657;left:10px;top:0px; " +
                        " cursor: pointer;padding:10px; ");
                break;
            default:
                closeButton.innerHTML = "<div style ='background: url(http://de5zarwna0j2q.cloudfront.net/site/images/videoCloseBtn.png); width: 16px; height: 16px; '></div>"
                closeButton.setAttribute("style", "position:absolute; z-index:12234;right:10px;top:10px; opacity: 0.7;cursor: pointer;padding:14px; ");
                break;
        }
        closeButton.onmouseover = function () {
            closeButton.style.opacity = "1";
        }
        closeButton.onmouseout = function () {
            closeButton.style.opacity = "0.7";
        }
        closeButton.onclick = function () {
            eaNative.closeOverlayContent();
        }
        return closeButton;
    }
    eaNative.getOverlayContainer = function (vid, contentType) {
        var container = win.document.getElementById("ea-overlay-content");
        if (!container) {
            container = win.document.createElement("div");
            container.innerHTML = "<div id='ea-overlay-wrapper' style='display: table;width:100%;height:100%;'><div id='ea-overlay-inner-container' oncontextmenu='return false' style='display: table-cell;vertical-align: middle;text-align: center;width:100%;height:100%;'></div></div>";
            container.setAttribute("id", "ea-overlay-content");
            container.setAttribute("style", "display:block;position:fixed;z-index: 3147483656;width:100%;height:100%;top:0;left:0;background:#000;background:rgba(0,0,0,0.8);");

            container.appendChild(eaNative.getOverlayCloseButton(contentType));
        }
        container.setAttribute('data-vid', vid);
        var wrapper = container.children[0];
        container.onclick = function (ev) {

            if (!ev)
                ev = window.event;
            var target = ev.target;
            if (!target)
                target = ev.srcElement;
            if (target == container.children[0] || target == wrapper.children[0]) {
                eaNative.closeOverlayContent();

                return false;
            }
        }
        win.document.body.appendChild(container);

        return wrapper.children[0];
    }

    ///tracking system
    eaNative.stopUITTracking = function () {
        clearInterval(eaNative.tuitTrackingInterval);
    }
    eaNative.startUITTracking = function (vid) {
        clearInterval(eaNative.tuitTrackingInterval);
        eaNative.tuitTrackingInterval = setInterval(function () {
            eaNative.trackUITEvent(vid);
        }, 1000);
    }
    eaNative.trackUITEvent = function (vid) {
        eaNative.uit += eaNative.uitInterval;
        if (eaNative.uit > 60000) {
            eaNative.uitInterval = 10000;
        } else if (eaNative.uit > 30000) {
            eaNative.uitInterval = 50000;
        }
        ;
        var adSettings = eaNative.nativeAdsSourceByVid[vid];
        this.trackEvent({vst: [{es: trackingEvents.general, tuit: eaNative.uitInterval}]}, adSettings, false);
    }

    eaNative.trackEvent = function (data, adSettings, isEventTracking, formatTypeId) {
        "use strict";
        if (!adSettings) {
            if (win.console && win.console.log) {
                win.console.log('EA Error: - tracking failed');
            }
            return;
        }
        ;
        if (adSettings.adnx && (eaNative.isIntegrationDSP[adSettings.ah] || adSettings.adnx.enableEATrack === false)) {
            return;
        }
        if (!formatTypeId) {
            formatTypeId = formatType[adSettings.type];
        }
        var trackedEvents = {};
        trackedEvents.h = "nuid." + EAdConfig.uid + "." + adSettings.vid;
        trackedEvents.type = data;
        trackedEvents.ft = formatTypeId;
        trackedEvents.ch = adSettings.ah;
        trackedEvents.uid = EAdConfig.uid;
        trackedEvents.pid = adSettings.pid;
        trackedEvents.dsp = adSettings.adnx.dsp;

        var adnx = adSettings;

        if (adSettings.adnx) {
            adnx = adSettings.adnx;
        }
        trackedEvents.feac = String(adnx.feac) === "true" ? 1 : 0;
        trackedEvents.country = adnx.country;
        trackedEvents.pubid = adnx.pubid;
        trackedEvents.sid = adnx.sid;
        trackedEvents.cpid = adnx.cpid;
        trackedEvents.cid = adnx.cid;
        trackedEvents.csize = adnx.csize;


        if (adSettings.performance && !win.ean.utils.isEmpty(adSettings.performance))
            trackedEvents.performance = {type: (isEventTracking == false ? "v" : "ev"), data: adSettings.performance};

        if (EAdConfig.debug == true) {
            if (win.console && win.console.log) {
                //win.console.log("TRACK:", trackedEvents, adSettings);
            }
        }
        var source = (URLPaths.trackingURL) + "?trk=" + B64.encode(JSON.stringify(trackedEvents));
        this.createNewPixel(source);
    }
    eaNative.createNewPixel = function (source, onComplete, onError) {
        "use strict";

        var type = "script";
        var pixelElement = win.document.createElement(type);
        pixelElement.setAttribute("type", "text/javascript")
        pixelElement.setAttribute("src", source);
        win.document.body.appendChild(pixelElement);
    }
    eaNative.setUID = function (uid) {
        if (!EAdConfig.uid && uid) {
            EAdConfig.uid = uid || 0;
        }
    }
    eaNative.loadPlacementData = function () {
        var articleId = win.ean.utils.getParameterByName("ean-article-id");
        var articlePID = win.ean.utils.getParameterByName("ean-article-pid");
        if (articleId && articlePID) {
            win.ean.utils.loadPlacementConfig(URLPaths.articlePlacemensConfigURL);
        }
        win.ean.utils.loadPlacementConfig(URLPaths.nativePlacementsConfigURL);
    }
    eaNative.init = function () {

        if (eaNative.initialized == true)
            return;


        eaNative.initialized = true;
        eaNative.setUID(win.ean.utils.getParameterByName("ean-uid"));
        eaNative.loadPlacementData();


    }
    eaNative.removePlacementForScrollingChecking = function (placement) {
        if (eaNative.placementsNotVisible.length > 0) {
            for (var i in eaNative.placementsNotVisible) {
                if (eaNative.placementsNotVisible[i] == placement) {
                    eaNative.placementsNotVisible.splice(i, 1);
                    break;
                }
            }
        }
        if (eaNative.placementsNotVisible.length == 0) {
            win[rem](pre + "scroll", eaNative.windowScrollHandler, false);
        }
    }
    eaNative.addPlacementForScrollingChecking = function (placement) {
        var placementAlreadyExist = false;
        if (eaNative.placementsNotVisible.length > 0) {
            for (var i in eaNative.placementsNotVisible) {
                if (eaNative.placementsNotVisible[i] == placement) {
                    placementAlreadyExist = true;
                }
            }
        }
        if (placementAlreadyExist == false) {
            eaNative.placementsNotVisible.push(placement);
        }
        if (eaNative.placementsNotVisible.length > 0 && placement.template.check_visibility_with_interval == true) {
            win.clearInterval(eaNative.checkPlacementVisibilityInterval);
            eaNative.checkPlacementVisibilityInterval = win.setInterval(eaNative.checkPlacementsNotVisibleHandler, 100);
        }
        win[eaNative.placementsNotVisible.length > 0 ? add : rem](pre + "scroll", eaNative.windowScrollHandler, false);

    }
    eaNative.windowScrollHandler = function () {
        win.clearTimeout(eaNative.placementCheckScrollInterval);
        eaNative.placementCheckScrollInterval = win.setTimeout(eaNative.checkPlacementsNotVisibleHandler, 100);
    }
    eaNative.checkPlacementsNotVisibleHandler = function () {
        if (eaNative.placementsNotVisible.length > 0) {
            eaNative.initPlacement(eaNative.placementsNotVisible);
        }
    }
    eaNative.removeVastVideosForScrollingChecking = function (vastvideo) {
        if (eaNative.videosOnAutoPlay.length > 0) {
            for (var i in eaNative.videosOnAutoPlay) {
                if (eaNative.videosOnAutoPlay[i] === vastvideo) {
                    eaNative.videosOnAutoPlay.splice(i, 1);
                    break;
                }
            }
        }
        if (eaNative.videosOnAutoPlay.length == 0) {
            win[rem](pre + "scroll", eaNative.windowVideoScrollHandler, false);
        }
    };
    eaNative.addVastVideosForScrollingChecking = function (vastvideo) {
        var vastvideoAlreadyExist = false;
        if (eaNative.videosOnAutoPlay.length > 0) {
            for (var i in eaNative.videosOnAutoPlay) {
                if (eaNative.videosOnAutoPlay[i] === vastvideo) {
                    vastvideoAlreadyExist = true;
                }
            }
        }
        if (vastvideoAlreadyExist === false) {
            eaNative.videosOnAutoPlay.push(vastvideo);
        }
        win[eaNative.videosOnAutoPlay.length > 0 ? add : rem](pre + "scroll", eaNative.windowVideoScrollHandler, false);
    };
    eaNative.addAdElForScrollingChecking = function (ad, data) {
        win[add](pre + "scroll", function () {
            eaNative.windowAdScrollHandler(ad, data)
        }, false);
    };
    eaNative.windowAdScrollHandler = function (ad, data) {
        win.clearTimeout(eaNative.vastAdsCheckScrollInterval);
        eaNative.vastAdsCheckScrollInterval = win.setTimeout(eaNative.checkAdsNotVisibleHandler(ad, data), 100);
    };
    eaNative.windowVideoScrollHandler = function () {
        win.clearTimeout(eaNative.vastVideosCheckScrollInterval);
        eaNative.vastVideosCheckScrollInterval = win.setTimeout(eaNative.checkVastVideosNotVisibleHandler, 100);
    };
    eaNative.checkAdsNotVisibleHandler = function (ad, data) {
        var beacons = ad.querySelectorAll('.ea-yahoo-integration-beacon');
        if (win.ean.utils.isElementVisiblePercent(ad, 50) && beacons.length < 1) {
            win.clearTimeout(eaNative.vastAdsExtrasScrollInterval);
            eaNative.vastAdsExtrasScrollInterval = win.setTimeout(function () {
                win.ean.dspLoadExtras[data.settings.ah](ad)
            }, 1000);
        }
    };
    eaNative.checkVastVideosNotVisibleHandler = function () {
        if (eaNative.videosOnAutoPlay.length > 0) {
            for (var i = 0; i < eaNative.videosOnAutoPlay.length; i++) {
                var vid = eaNative.videosOnAutoPlay[i].getAttribute('data-vid');

                if(EAdConfig.debug === true) {
                    win.console.log("Video: " + vid + " scroll visibility: " + (100 - eaNative.getVisiblePercentForAd(vid)));
                }

                if (
                    win.ean.utils.isElementVisiblePercent(
                        eaNative.videosOnAutoPlay[i],
                        eaNative.getVisiblePercentForAd(vid)
                    ) &&
                    eaNative.videosOnAutoPlay[i].querySelector('iframe').contentWindow)
                {
                    eaNative.videosOnAutoPlay[i].querySelector('iframe').contentWindow.postMessage('ea_video_play|' + vid, '*');
                } else if (eaNative.videosOnAutoPlay[i].querySelector('iframe').contentWindow) {
                    eaNative.videosOnAutoPlay[i].querySelector('iframe').contentWindow.postMessage('ea_video_pause|' + vid, '*');
                }
            }
        }
    };
    eaNative.getVisiblePercentForAd = function(vid) {
        var nativeAd    = eaNative.nativeAdsSourceByVid[vid];
        var viewability = 50;

        if(!nativeAd.hasOwnProperty('tags')) {
            return viewability;
        }

        if(
            nativeAd.tags.hasOwnProperty('expand_viewability') &&
            parseInt(nativeAd.tags.expand_viewability) > 0 &&
            nativeAd.hasOwnProperty('expand_vid') &&
            nativeAd.expand_vid === vid
        ) {
            return 100 - parseInt(nativeAd.tags.expand_viewability);
        }

        if(
            nativeAd.tags.hasOwnProperty('viewability') &&
            !isNaN(parseInt(nativeAd.tags.viewability)) &&
            parseInt(nativeAd.tags.viewability) > 0 &&
            nativeAd.vid === vid
        ) {
            return 100 - parseInt(nativeAd.tags.viewability);
        }

        return viewability;
    };
    eaNative.startPostMessageTracking = function () {
        function receiveMessage(event) {
            if (!event.data || !event.data.indexOf) {
                return;
            }
            if (event.data && event.data.indexOf('ea_track_') > -1) {
                var vid = event.data.split('|')[1];
                var adSettings = eaNative.nativeAdsSourceByVid[vid];
                eaNative.trackEvent({ev: [{a: trackingEvents.click, es: trackingEvents.general}]}, adSettings);
                eaNative.trackClickTag(adSettings);
            }
            if (event.data && event.data.indexOf('expandVid_') > -1) {
                eaNative.expandVideoTeaser(event.data);
            }
            if (event.data && event.data.indexOf('closeVid_') > -1) {
                eaNative.closeVideoTeaser(event.data);
            }
        }
        if (win.addEventListener) {
            win.addEventListener("message", receiveMessage, false);
        } else {
            win.attachEvent("message", receiveMessage);
        }
        eaNative.postMesageTrackingEnabled = true;
    };
    eaNative.clickExpandHide = function (adSettings) {
        var closeButton = win.document.querySelector(".ea_expand_close[data-vid='" + adSettings.vid + "']");
        if (closeButton != null) {
            closeButton.style.display = "block";
        }

        var summaryContainer = win.document.querySelector(".ea_summary[data-vid='" + adSettings.vid + "']");

        win.ean.utils.insertHTML(summaryContainer, adSettings.tags.summary);

        var expandedSummaryContainer = win.document.querySelector(
            ".ea_expanded_summary[data-vid='" + adSettings.expand_vid + "']"
        );

        if (expandedSummaryContainer) {
            var otherParam = (adSettings.tags.vast_pause_option == 1) ? adSettings.expand_vid : "";
            win.ean.utils.insertHTML(expandedSummaryContainer, eaNative.getHTMLTag(adSettings.tags, "expanded_summary", otherParam));
            if (adSettings.tags.vast_pause_option == 1) {
                eaNative.postMesageTrackingEnabled && eaNative.startPostMessageTracking(adSettings);
                eaNative.addVastVideosForScrollingChecking(expandedSummaryContainer);
            }

            expandedSummaryContainer.style.display = "";

            eaNative.parseScripts(expandedSummaryContainer.children);
        }

        var expandLinks = win.document.querySelectorAll(".ea_expand_hide[data-vid='" + adSettings.vid + "']");
        for (var i = 0; i < expandLinks.length; i++) {
            expandLinks[i].style.display = "none";
        }

        eaNative.trackEvent({v: [{es: trackingEvents.general}]}, adSettings, false, formatType.expanded);
        eaNative.measureAndSendSizeEvent(adSettings.adnx);
    };
    eaNative.expandVideoTeaser = function (vid) {
        var video = win.document.querySelector('.ea_video_vast[data-vid="' + vid.split('_')[1] + '"]');
        var videoNativeEl = win.ean.utils.getParentByClass(video, 'ea_native_element');

        if (videoNativeEl.className.indexOf('ea_click_expand_video') === -1) {
            videoNativeEl.className += " ea_click_expand_video";
        }
    };
    eaNative.closeVideoTeaser = function (vid) {
        var video = win.document.querySelector('.ea_video_vast[data-vid="' + vid.split('_')[1] + '"]');
        var videoNativeEl = win.ean.utils.getParentByClass(video, 'ea_native_element');

        videoNativeEl.className = videoNativeEl.className.replace('ea_click_expand_video', '');
    };
    eaNative.sendSizeEvent = function (w, h) {
        if (win.parent) {
            win.parent.postMessage('size_' + w + "x" + h, '*');
        }
    };
    eaNative.measureAndSendSizeEvent = function (adSettings) {
        if ((String(adSettings.isInPreview) == "true" || String(adSettings.isInPreview) == "1") && win.parent) {

            var selector = adSettings.placementConfig.template.selector;

            if (!win.ean.utils.isArray(selector)) {
                selector = [selector];
            }
            var adContainer = null;
            for (var i in selector) {
                adContainer = doc.querySelector(selector[i]);
                if (adContainer != null)
                    break;
            }
            if (adContainer) {
                eaNative.sendSizeEvent(adContainer.offsetWidth, adContainer.offsetHeight);
            }
        }
    };

    eaNative.callRetargetingPixels = function () {
        var retargetingShtPixels1 = win.location.protocol + '//cdn.elasticad.net/native/serve/js/custom/custom.gz.js';
        win.ean.utils.loadScript(retargetingShtPixels1);
//        var retargetingShtPixels2 = doc.createElement('iframe');
//        retargetingShtPixels2.style.display = 'none';
//        retargetingShtPixels2.width = 0;
//        retargetingShtPixels2.height = 0;
//        retargetingShtPixels2.frameBorder = 0;
//        retargetingShtPixels2.src = win.location.protocol + '//tag.leadplace.fr/wckr.php?id=HIM2';
//        doc.body.appendChild(retargetingShtPixels2);
    };

    //bsae64 --------------------------------------------------------------------------------------------------
    var B64 = {
        alphabet: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',
        lookup: null,
        ie: /MSIE /.test(navigator.userAgent),
        ieo: /MSIE [67]/.test(navigator.userAgent),
        encode: function (s) {
            var buffer = B64.toUtf8(s),
                    position = -1,
                    len = buffer.length,
                    nan1, nan2, enc = [, , , ];
            if (B64.ie) {
                var result = [];
                while (++position < len) {
                    nan1 = buffer[position + 1], nan2 = buffer[position + 2];
                    enc[0] = buffer[position] >> 2;
                    enc[1] = ((buffer[position] & 3) << 4) | (buffer[++position] >> 4);
                    if (isNaN(nan1))
                        enc[2] = enc[3] = 64;
                    else {
                        enc[2] = ((buffer[position] & 15) << 2) | (buffer[++position] >> 6);
                        enc[3] = (isNaN(nan2)) ? 64 : buffer[position] & 63;
                    }
                    result.push(B64.alphabet[enc[0]], B64.alphabet[enc[1]], B64.alphabet[enc[2]], B64.alphabet[enc[3]]);
                }
                return result.join('');
            } else {
                result = '';
                while (++position < len) {
                    nan1 = buffer[position + 1], nan2 = buffer[position + 2];
                    enc[0] = buffer[position] >> 2;
                    enc[1] = ((buffer[position] & 3) << 4) | (buffer[++position] >> 4);
                    if (isNaN(nan1))
                        enc[2] = enc[3] = 64;
                    else {
                        enc[2] = ((buffer[position] & 15) << 2) | (buffer[++position] >> 6);
                        enc[3] = (isNaN(nan2)) ? 64 : buffer[position] & 63;
                    }
                    result += B64.alphabet[enc[0]] + B64.alphabet[enc[1]] + B64.alphabet[enc[2]] + B64.alphabet[enc[3]];
                }
                return result;
            }
        },
        toUtf8: function (s) {
            var position = -1,
                    len = s.length,
                    chr, buffer = [];
            if (/^[\x00-\x7f]*$/.test(s))
                while (++position < len)
                    buffer.push(s.charCodeAt(position));
            else
                while (++position < len) {
                    chr = s.charCodeAt(position);
                    if (chr < 128)
                        buffer.push(chr);
                    else if (chr < 2048)
                        buffer.push((chr >> 6) | 192, (chr & 63) | 128);
                    else
                        buffer.push((chr >> 12) | 224, ((chr >> 6) & 63) | 128, (chr & 63) | 128);
                }
            return buffer;
        }
    };
    eaNative.B64 = B64;


    //wait for document ready to init
    var initialized = false,
            add = doc.addEventListener ? "addEventListener" : "attachEvent",
            rem = doc.addEventListener ? "removeEventListener" : "detachEvent",
            pre = doc.addEventListener ? "" : "on";
    var isReady = function () {
        var s = doc.readyState;

        if (s === 'complete' || s === 'loaded') {
            return true;
        }
        if (s != 'interactive')
            return false;
        if (!doc.documentElement.doScroll)
            return true;
        try {
            doc.documentElement.doScroll('left');
            return true;
        } catch (e) {
            return false;
        }
    }
    var isDocumentReady = function () {
        var readyState = doc.readyState;

        if (isReady())
            return true;

        return  ((readyState === 'interactive' && navigator.appName.indexOf("Internet Explorer") == -1)
                || readyState === 'loaded' || readyState === 'complete');

    };
    var documentLoadHandler = function () {
        if (isDocumentReady()) {
            win.adnxsDomReady = true;
            doc[rem](pre + "DOMContentLoaded", documentLoadHandler, false);
            doc[rem](pre + "readystatechange", documentLoadHandler, false);
            win[rem](pre + "load", documentLoadHandler, false);

            windowReady();

            return true;
        }
        return false;
    };
    var windowReady = function () {
        if (!eaNative.ean_window_ready) {
            eaNative.ean_window_ready = true;
            win.clearInterval(eaNative.ieTimeout);
            eaNative.init();
        }
    };
    var getIEVersion = function () {
        var rv = -1; // Return value assumes failure.
        if (navigator.appName == 'Microsoft Internet Explorer')
        {
            var ua = navigator.userAgent;
            var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) != null)
                rv = parseFloat(RegExp.$1);
        }
        return rv;
    }

    if (isDocumentReady()) {
        windowReady();
    } else {
        doc[add](pre + "DOMContentLoaded", documentLoadHandler, false);
        doc[add](pre + "readystatechange", documentLoadHandler, false);
        win[add](pre + "load", documentLoadHandler, false);

        var ieVersion = getIEVersion();
        if (ieVersion > -1 && ieVersion < 9) {
            eaNative.ieTimeout = win.setInterval(function () {
                if (documentLoadHandler()) {
                    win.clearInterval(eaNative.ieTimeout);
                }
                ;
            }, 500);
        }
    }
    ;
    win.ean.loaded = true;


})(window, document);
