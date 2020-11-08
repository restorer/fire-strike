var toolbar = (function() {
	return {
		onDraw: function() {
			selection.active = false;
			selection.update();
			app.setMode(app.MODE_DRAW);
		},

		onSelect: function() {
			if (app.mode != app.MODE_SELECT) {
				app.setMode(app.MODE_SELECT);
			}
		},

		onDeselect: function() {
			if (selection.active) {
				selection.active = false;
				selection.update();
				app.setMode(app.MODE_DRAW);
			}
		},

		onCopy: function() {
			if (selection.active && app.mode != app.MODE_COPY) {
				map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);
				app.setMode(app.MODE_COPY);
			}
		},

		onMove: function() {
			if (selection.active && app.mode != app.MODE_MOVE) {
				map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);
				app.setMode(app.MODE_MOVE);
			}
		},

		onFill: function() {
			if (!selection.active) {
				return;
			}

			map.tmpMap = null;

			for (var i = selection.sy; i <= selection.ey; i++) {
				for (var j = selection.sx; j <= selection.ex; j++) {
					map.setCellValue(j, i);
				}
			}

			if (map.tmpMap != null) {
				map.addToUndo(map.tmpMap);
				map.tmpMap = null;
				map.update(selection.sx, selection.sy, selection.w, selection.h);
			}

			if (app.mode != app.MODE_SELECT) {
				app.setMode(app.MODE_SELECT);
			}
		},

		onFlipHor: function() {
			if (!selection.active) {
				return;
			}

			map.tmpMap = map.getMapCopy();
			map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);

			for (var i = selection.sy; i <= selection.ey; i++) {
				for (var j = selection.sx; j <= selection.ex; j++) {
					cell.copyToFrom(map.m[i][j], map.copyBuffer[i - selection.sy][selection.w - (j - selection.sx) - 1]);
				}
			}

			map.addToUndo(map.tmpMap);
			map.tmpMap = null;
			map.update(selection.sx, selection.sy, selection.w, selection.h);

			if (app.mode != app.MODE_SELECT) {
				app.setMode(app.MODE_SELECT);
			}
		},

		onFlipVert: function() {
			if (!selection.active) {
				return;
			}

			map.tmpMap = map.getMapCopy();
			map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);

			for (var i = selection.sy; i <= selection.ey; i++) {
				for (var j = selection.sx; j <= selection.ex; j++) {
					cell.copyToFrom(map.m[i][j], map.copyBuffer[selection.h - (i - selection.sy) - 1][j - selection.sx]);
				}
			}

			map.addToUndo(map.tmpMap);
			map.tmpMap = null;
			map.update(selection.sx, selection.sy, selection.w, selection.h);

			if (app.mode != app.MODE_SELECT) {
				app.setMode(app.MODE_SELECT);
			}
		},

		onToggleMark: function() {
			app.setMode(app.mode == app.MODE_MARK ? app.MODE_DRAW : app.MODE_MARK);
		},

		onMarkClear: function() {
			lib.query('.b-mark-value')
				.set('value', '')
				.exec('focus');
		},

		onUndo: function() {
			if (map.undoPos <= 0) {
				return;
			}

			if (map.undoPos == map.undoBuffer.length) {
				if (map.addToUndo(map.getMapCopy(), map.undoBuffer[map.undoPos - 1])) {
					map.undoPos--;
				}
			}

			map.undoPos--;
			map.copyMapFrom(map.undoBuffer[map.undoPos]);
		},

		onRedo: function() {
			if ((map.undoPos + 1) < map.undoBuffer.length) {
				map.undoPos++;
				map.copyMapFrom(map.undoBuffer[map.undoPos]);
			}
		},

		onToggleOptions: function() {
			var willActive = !lib.query('.b-options').hasClass('active');
			lib.query('.b-options').addRemoveClass(willActive, 'active');
			lib.query('.b-options-sub').toggle(willActive);
			file.hideSubDialog();
		},

		onToggleSaveLoad: function() {
			var willActive = !lib.query('.b-save-load').hasClass('active');
			lib.query('.b-save-load').addRemoveClass(willActive, 'active');
			lib.query('.b-save-load-sub').toggle(willActive);
			options.hideSubDialog();
		},

		onMapZoom: function(zoom) {
			lib.query('.b-map-zoom').removeClass('active');
			lib.query('.b-map-zoom[rel="' + zoom + '"]').addClass('active');
			lib.query('.map').set('className', 'map ' + (zoom === 4 ? 'map-z25' : (zoom === 2 ? 'map-z50' : '')));

			map.zoomMult = zoom;
			map.zoomCellSize = 64 / map.zoomMult;
		},

		onMapRenderMode: function(mode) {
			lib.query('.b-map-rendermode').removeClass('active');
			lib.query('.b-map-rendermode[rel="' + mode + '"]').addClass('active');
			map.setRenderMode(mode);
		},

		onSidebarZoom: function(zoom) {
			lib.query('.b-sidebar-zoom').removeClass('active');
			lib.query('.b-sidebar-zoom[rel="' + zoom + '"]').addClass('active');
			lib.query('.sidebar').set('className', 'sidebar ' + zoom);
		},

		init: function() {
			lib.query('.b-draw').on('click', toolbar.onDraw);
			lib.query('.b-select').on('click', toolbar.onSelect);
			lib.query('.b-deselect').on('click', toolbar.onDeselect);
			lib.query('.b-copy').on('click', toolbar.onCopy);
			lib.query('.b-move').on('click', toolbar.onMove);
			lib.query('.b-fill').on('click', toolbar.onFill);
			lib.query('.b-flip-hor').on('click', toolbar.onFlipHor);
			lib.query('.b-flip-ver').on('click', toolbar.onFlipVert);
			lib.query('.b-mark').on('click', toolbar.onToggleMark);
			lib.query('.b-mark-clear').on('click', toolbar.onMarkClear);
			lib.query('.b-undo').on('click', toolbar.onUndo);
			lib.query('.b-redo').on('click', toolbar.onRedo);
			lib.query('.b-options').on('click', toolbar.onToggleOptions);
			lib.query('.b-save-load').on('click', toolbar.onToggleSaveLoad);

			lib.query('.b-map-zoom').on('click', function() {
				toolbar.onMapZoom(parseInt(this.getAttribute('rel'), 10));
			});

			lib.query('.b-map-rendermode').on('click', function() {
				toolbar.onMapRenderMode(this.getAttribute('rel'));
			});

			lib.query('.b-sidebar-zoom').on('click', function() {
				toolbar.onSidebarZoom(this.getAttribute('rel'));
			});

			app.keyboardShortcuts['KeyW'] = toolbar.onDraw;
			app.keyboardShortcuts['KeyS'] = toolbar.onSelect;
			app.keyboardShortcuts['KeyD'] = toolbar.onDeselect;
			app.keyboardShortcuts['KeyC'] = toolbar.onCopy;
			app.keyboardShortcuts['KeyV'] = toolbar.onMove;
			app.keyboardShortcuts['KeyI'] = toolbar.onFill;
			app.keyboardShortcuts['KeyK'] = toolbar.onToggleMark;

			app.keyboardShortcuts['Digit2'] = function() {
				toolbar.onMapZoom(4);
			};

			app.keyboardShortcuts['Digit5'] = function() {
				toolbar.onMapZoom(2);
			};

			app.keyboardShortcuts['Digit0'] = function() {
				toolbar.onMapZoom(1);
			};
		}
	};
})();
