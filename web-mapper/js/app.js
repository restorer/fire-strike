var app = (function() {
	function initCommon() {
	}

	return {
		MODE_DRAW: 'draw',
		MODE_SELECT: 'select',
		MODE_COPY: 'copy',
		MODE_MOVE: 'move',
		MODE_MARK: 'mark',

		T_HERO: 1,
		T_WALL: 2,
		T_TWALL: 3,
		// T_TPASS: 4,
		T_TWIND: 5,
		T_DOOR: 6,
		T_DITEM: 7,
		T_DLAMP: 8,
		T_OBJ: 9,
		T_MON: 10,
		T_EMPTY: 100,
		T_JUST_FLOOR: 101,
		T_NO_TRANS: 102,
		T_FLOOR: 103,
		T_CEIL: 104,
		T_DOOR_S: 105,
		T_ARROW: 106,
		T_NO_CEIL: 107,
		T_FLOOR_SEL: 108,
		T_FLOOR_DISP: 109,
		T_CEIL_SEL: 110,
		T_CEIL_DISP: 111,
		T_NO_CEIL: 112,

		mode: '',
		selectedTool: null,
		specialFloor: [ 1, 1, 1, 1 ],
		specialCeil: [ 1, 1, 1, 1 ],
		keyboardShortcuts: {},

		start: function() {
			app.init();
			paths.init();
			toolbar.init();
			sidebar.init();
			options.init();
			map.init();
			selection.init();
			file.init();
		},

		init: function() {
			app.mode = app.MODE_DRAW;

			document.onselectstart = function(event) {
				switch ((event.target && event.target.tagName) ? event.target.tagName.toUpperCase() : '') {
					case 'INPUT':
					case 'TEXTAREA':
						return;

					default:
						return false;
				}
			};

			document.ondragstart = function(event) {
				return false;
			};

			window.onkeydown = function(event) {
				var isOptionsDialogVisible = lib.query('.b-options').hasClass('active');
				var isSaveLoadDialogVisible = lib.query('.b-save-load').hasClass('active');
				var isMarkDialogVisible = (app.mode == app.MODE_MARK);

				if (isOptionsDialogVisible || isSaveLoadDialogVisible || isMarkDialogVisible) {
					if (event.code !== 'Escape') {
						return;
					}

					if (isOptionsDialogVisible) {
						toolbar.onToggleOptions();
					}

					if (isSaveLoadDialogVisible) {
						toolbar.onToggleSaveLoad();
					}

					if (isMarkDialogVisible) {
						toolbar.onToggleMark();
					}

					return false;
				}

				var shortcutCb = app.keyboardShortcuts[event.code];

				if (shortcutCb) {
					shortcutCb();
					return false;
				}
			};
		},

		setMode: function(mode) {
			app.mode = mode;

			lib.query('.b-mode').removeClass('active');
			lib.query('.b-mode-sub').hide();

			lib.query('.b-' + mode).addClass('active');
			lib.query('.b-' + mode + '-sub').show();
			lib.query('.b-' + mode + '-focus').exec('focus');

			lib.query('.b-depend-selection').addRemoveClass(!selection.active, 'disabled');

			if ((mode == app.MODE_COPY || mode == app.MODE_MOVE) && selection.active) {
				map.cursorElementStyle.width = (selection.w * 64) + 'px';
				map.cursorElementStyle.height = (selection.h * 64) + 'px';
			} else {
				map.cursorElementStyle.width = '64px';
				map.cursorElementStyle.height = '64px';
			}
		}
	};
})();
